package br.com.droneshop.compra.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.compra.dto.CarrinhoResponseDTO;
import br.com.droneshop.compra.dto.ItemCarrinhoRequestDTO;
import br.com.droneshop.compra.dto.ProdutoResumoDTO;
import br.com.droneshop.compra.exception.CupomRecusadoException;
import br.com.droneshop.compra.model.Carrinho;
import br.com.droneshop.compra.model.Cupom;
import br.com.droneshop.compra.model.ItemCarrinho;
import br.com.droneshop.compra.model.StatusCarrinho;
import br.com.droneshop.compra.repository.CarrinhoRepository;
import br.com.droneshop.compra.repository.CupomRepository;
import br.com.droneshop.compra.repository.UsoCupomRepository;
import br.com.droneshop.estoque.model.Reserva;
import br.com.droneshop.estoque.model.StatusReserva;
import br.com.droneshop.estoque.service.ReservaSaldo;
import br.com.droneshop.estoque.service.ReservaService;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.repository.PerfilClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CarrinhoServiceImpl implements CarrinhoService {

    @ConfigProperty(name = "aeria.carrinho.abandono-horas", defaultValue = "24")
    int horasAbandono;

    @Inject
    Event<CarrinhoAbandonado> carrinhoAbandonado;

    @Inject
    CarrinhoRepository carrinhos;

    @Inject
    ProdutoRepository produtos;

    @Inject
    PerfilClienteRepository clientes;

    @Inject
    CupomRepository cupons;

    @Inject
    UsoCupomRepository usos;

    @Inject
    ReservaService reservas;

    @Override
    @Transactional
    public CarrinhoResponseDTO criar() {
        return resposta(novo(null));
    }

    private Carrinho novo(PerfilCliente cliente) {
        Carrinho c = new Carrinho();
        c.setTokenSessao(UUID.randomUUID().toString());
        c.setStatus(StatusCarrinho.ATIVO);
        c.setCliente(cliente);
        c.setUltimaAlteracao(LocalDateTime.now());
        carrinhos.persist(c);
        return c;
    }

    private Carrinho porToken(String token) {
        Carrinho c = carrinhos.porToken(token);
        if (c == null)
            throw new ResourceNotFoundException("Carrinho não encontrado.");
        return c;
    }

    @Override
    public Carrinho ativo(String token) {
        Carrinho c = porToken(token);
        if (c.getStatus() != StatusCarrinho.ATIVO)
            throw new ConflitoException("Este carrinho não está mais ativo.");
        return c;
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO buscar(String token) {
        Carrinho c = porToken(token);
        List<String> avisos = c.getStatus() == StatusCarrinho.ATIVO ? recalcular(c) : List.of();
        return resposta(c, avisos);
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO adicionar(String token, ItemCarrinhoRequestDTO dto) {
        Carrinho c = ativo(token);
        Produto p = produtos.findById(dto.idProduto());
        if (p == null || p.getStatus() != StatusProduto.ATIVO || p.getPreco() == null)
            throw ValidationException.of("idProduto", "Produto indisponível para venda (back)");
        ItemCarrinho item = c.getItens().stream().filter(i -> i.getProduto().getId().equals(p.getId()))
                .findFirst().orElse(null);
        int quantidade = dto.quantidade() + (item == null ? 0 : item.getQuantidade());
        conferirDisponivel(p, quantidade, item);
        if (item == null) {
            item = new ItemCarrinho();
            item.setCarrinho(c);
            item.setProduto(p);
            item.setPrecoUnitarioMomento(p.getPreco());
            c.getItens().add(item);
        }
        item.setQuantidade(quantidade);
        return alterado(c);
    }

    // O que o próprio item já reservou volta para a conta (renovar não pode faltar saldo)
    private void conferirDisponivel(Produto p, int quantidade, ItemCarrinho item) {
        int meu = item == null ? 0
                : item.getReservas().stream()
                        .filter(r -> r.getStatus() == StatusReserva.ATIVA && r.getProduto().getId().equals(p.getId()))
                        .mapToInt(Reserva::getQuantidade).sum();
        int livre = ReservaSaldo.disponivel(p) + meu;
        if (quantidade > livre)
            throw new ConflitoException("Só há " + livre + " unidade(s) disponível(is) de " + p.getNome() + ".");
    }

    private ItemCarrinho item(Carrinho c, Long idItem) {
        return c.getItens().stream().filter(i -> i.getId().equals(idItem)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado no carrinho."));
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO alterarQuantidade(String token, Long idItem, int quantidade) {
        Carrinho c = ativo(token);
        ItemCarrinho item = item(c, idItem);
        conferirDisponivel(item.getProduto(), quantidade, item);
        item.setQuantidade(quantidade);
        return alterado(c);
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO remover(String token, Long idItem) {
        Carrinho c = ativo(token);
        ItemCarrinho item = item(c, idItem);
        reservas.liberar(List.of(item));
        item.getReservas().forEach(r -> r.setItemCarrinho(null));
        item.getReservas().clear();
        c.getItens().remove(item);
        return alterado(c);
    }

    private CarrinhoResponseDTO alterado(Carrinho c) {
        c.setUltimaAlteracao(LocalDateTime.now());
        carrinhos.flush();
        return resposta(c, recalcular(c));
    }

    /** Atualiza o preço do momento e confere o cupom; devolve os avisos (#25). */
    private List<String> recalcular(Carrinho c) {
        List<String> avisos = new ArrayList<>();
        for (ItemCarrinho i : c.getItens()) {
            BigDecimal atual = i.getProduto().getPreco();
            if (atual != null && atual.compareTo(i.getPrecoUnitarioMomento()) != 0)
                i.setPrecoUnitarioMomento(atual);
        }
        if (c.getCupomAplicado() != null) {
            try {
                descontoDoCupom(c, c.getCupomAplicado(), null);
            } catch (CupomRecusadoException e) {
                c.setCupomAplicado(null);
                avisos.add(CupomCalculo.VALOR_MINIMO.equals(e.getMotivo()) ? "CUPOM_REMOVIDO_VALOR_MINIMO"
                        : "CUPOM_REMOVIDO_" + e.getMotivo());
            }
        }
        return avisos;
    }

    // Sem cliente (carrinho anônimo), o limite por cliente fica para o checkout (RN03)
    private BigDecimal descontoDoCupom(Carrinho c, Cupom cupom, Long idUsuario) {
        PerfilCliente cliente = idUsuario != null ? clientes.porUsuario(idUsuario) : c.getCliente();
        Long usosCliente = cliente == null ? null : usos.countByCupomECliente(cupom.getId(), cliente.getId());
        return CupomCalculo.desconto(cupom, c.getItens(), usos.countByCupom(cupom.getId()), usosCliente,
                LocalDateTime.now());
    }

    @Override
    @Transactional
    public BigDecimal desconto(Carrinho c, Long idUsuario) {
        return c.getCupomAplicado() == null ? BigDecimal.ZERO : descontoDoCupom(c, c.getCupomAplicado(), idUsuario);
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO vincular(Long idUsuario, String token) {
        Carrinho anonimo = porToken(token);
        PerfilCliente cliente = clientes.porUsuario(idUsuario);
        if (cliente == null)
            throw new ResourceNotFoundException("Cliente não encontrado.");
        Carrinho doCliente = carrinhos.ativoDoUsuario(idUsuario);
        if (anonimo.getStatus() != StatusCarrinho.ATIVO || anonimo == doCliente)
            return doCliente != null ? resposta(doCliente) : doCliente(idUsuario);
        if (anonimo.getCliente() != null && !anonimo.getCliente().getId().equals(cliente.getId()))
            throw new ResourceNotFoundException("Carrinho não encontrado.");
        if (doCliente == null) {
            anonimo.setCliente(cliente);
            return alterado(anonimo);
        }
        for (ItemCarrinho i : anonimo.getItens()) {
            ItemCarrinho igual = doCliente.getItens().stream()
                    .filter(x -> x.getProduto().getId().equals(i.getProduto().getId())).findFirst().orElse(null);
            if (igual != null) {
                igual.setQuantidade(igual.getQuantidade() + i.getQuantidade());
            } else {
                ItemCarrinho novo = new ItemCarrinho();
                novo.setCarrinho(doCliente);
                novo.setProduto(i.getProduto());
                novo.setQuantidade(i.getQuantidade());
                novo.setPrecoUnitarioMomento(i.getPrecoUnitarioMomento());
                doCliente.getItens().add(novo);
            }
        }
        reservas.liberar(anonimo.getItens());
        if (doCliente.getCupomAplicado() == null)
            doCliente.setCupomAplicado(anonimo.getCupomAplicado());
        anonimo.setStatus(StatusCarrinho.EXPIRADO);
        return alterado(doCliente);
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO doCliente(Long idUsuario) {
        Carrinho c = carrinhos.ativoDoUsuario(idUsuario);
        if (c == null) {
            PerfilCliente cliente = clientes.porUsuario(idUsuario);
            if (cliente == null)
                throw new ResourceNotFoundException("Cliente não encontrado.");
            c = novo(cliente);
        }
        return resposta(c, recalcular(c));
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO reservar(String token) {
        Carrinho c = ativo(token);
        reservas.reservar(c.getItens());
        carrinhos.flush();
        return resposta(c);
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO aplicarCupom(String token, String codigo, Long idUsuario) {
        Carrinho c = ativo(token);
        recalcular(c);
        Cupom cupom = cupons.findByCodigo(codigo.trim());
        if (cupom == null)
            throw new CupomRecusadoException(CupomCalculo.INEXISTENTE, "Cupom não encontrado.");
        descontoDoCupom(c, cupom, idUsuario);
        c.setCupomAplicado(cupom);
        c.setUltimaAlteracao(LocalDateTime.now());
        return resposta(c);
    }

    @Override
    @Transactional
    public CarrinhoResponseDTO removerCupom(String token) {
        Carrinho c = ativo(token);
        c.setCupomAplicado(null);
        return alterado(c);
    }

    @Override
    @Transactional
    public int marcarAbandonados(LocalDateTime referencia) {
        int n = 0;
        for (Carrinho c : carrinhos.ativosParadosDesde(referencia.minusHours(horasAbandono))) {
            if (c.getItens().isEmpty())
                continue;
            reservas.liberar(c.getItens());
            c.setStatus(StatusCarrinho.ABANDONADO);
            carrinhoAbandonado.fire(new CarrinhoAbandonado(c.getId()));
            n++;
        }
        return n;
    }

    @Override
    public CarrinhoResponseDTO resposta(Carrinho c) {
        return resposta(c, List.of());
    }

    private CarrinhoResponseDTO resposta(Carrinho c, List<String> avisos) {
        List<CarrinhoResponseDTO.Item> itens = c.getItens().stream().map(this::item).toList();
        BigDecimal subtotal = CupomCalculo.subtotal(c.getItens());
        BigDecimal desconto = BigDecimal.ZERO;
        CarrinhoResponseDTO.CupomAplicado cupom = null;
        if (c.getCupomAplicado() != null && c.getStatus() == StatusCarrinho.ATIVO) {
            try {
                desconto = descontoDoCupom(c, c.getCupomAplicado(), null);
                cupom = new CarrinhoResponseDTO.CupomAplicado(c.getCupomAplicado().getCodigo(), desconto);
            } catch (CupomRecusadoException e) {
                desconto = BigDecimal.ZERO;
            }
        }
        return new CarrinhoResponseDTO(c.getTokenSessao(), c.getStatus(), itens, dinheiro(subtotal),
                dinheiro(desconto), dinheiro(subtotal.subtract(desconto)), cupom, avisos);
    }

    private static BigDecimal dinheiro(BigDecimal v) {
        return v.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private CarrinhoResponseDTO.Item item(ItemCarrinho i) {
        Produto p = i.getProduto();
        List<CarrinhoResponseDTO.Reserva> rs = i.getReservas().stream()
                .filter(r -> r.getStatus() == StatusReserva.ATIVA)
                .map(r -> new CarrinhoResponseDTO.Reserva(r.getId(),
                        new CarrinhoResponseDTO.ProdutoRef(r.getProduto().getId()), r.getQuantidade(), r.getStatus(),
                        r.getExpiraEm()))
                .toList();
        return new CarrinhoResponseDTO.Item(i.getId(),
                new CarrinhoResponseDTO.Produto(p.getId(), p.getSku(), p.getNome(), p.getSlug()), i.getQuantidade(),
                i.getPrecoUnitarioMomento(), i.getPrecoUnitarioMomento().multiply(BigDecimal.valueOf(i.getQuantidade())),
                ProdutoResumoDTO.disponivel(p), rs);
    }
}

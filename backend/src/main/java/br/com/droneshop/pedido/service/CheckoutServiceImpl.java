package br.com.droneshop.pedido.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.droneshop.catalogo.model.EscopoPromocao;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.Promocao;
import br.com.droneshop.catalogo.repository.PromocaoRepository;
import br.com.droneshop.compra.model.Carrinho;
import br.com.droneshop.compra.model.Cupom;
import br.com.droneshop.compra.model.ItemCarrinho;
import br.com.droneshop.compra.model.StatusCarrinho;
import br.com.droneshop.compra.model.UsoCupom;
import br.com.droneshop.compra.repository.UsoCupomRepository;
import br.com.droneshop.compra.service.CarrinhoService;
import br.com.droneshop.endereco.model.Endereco;
import br.com.droneshop.estoque.model.StatusReserva;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.EnderecoCliente;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.TipoPessoa;
import br.com.droneshop.identidade.repository.EnderecoClienteRepository;
import br.com.droneshop.identidade.repository.PerfilClienteRepository;
import br.com.droneshop.model.OrigemRegistro;
import br.com.droneshop.pagamento.model.MetodoPagamento;
import br.com.droneshop.pedido.dto.CheckoutRequestDTO;
import br.com.droneshop.pedido.dto.CotacaoFreteResponseDTO;
import br.com.droneshop.pedido.model.DescontoPedido;
import br.com.droneshop.pedido.model.Entrega;
import br.com.droneshop.pedido.model.EnderecoPedido;
import br.com.droneshop.pedido.model.HistoricoStatusPedido;
import br.com.droneshop.pedido.model.ItemPedido;
import br.com.droneshop.pedido.model.ModalidadeEntrega;
import br.com.droneshop.pedido.model.OrigemDesconto;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;

/** Fecha o carrinho em pedido PENDENTE: itens, endereço e entrega congelados (#26), descontos e cupom (#29). */
@ApplicationScoped
public class CheckoutServiceImpl implements CheckoutService {

    @Inject
    CarrinhoService carrinhos;

    @Inject
    FreteService frete;

    @Inject
    PerfilClienteRepository clientes;

    @Inject
    EnderecoClienteRepository enderecos;

    @Inject
    PromocaoRepository promocoes;

    @Inject
    UsoCupomRepository usosCupom;

    @Inject
    PedidoRepository pedidos;

    @Override
    @Transactional
    public Pedido fechar(Long idUsuario, String tokenCarrinho, CheckoutRequestDTO dto) {
        PerfilCliente cliente = clientes.porUsuario(idUsuario);
        if (cliente == null)
            throw new NotAuthorizedException("Cliente não encontrado.", "Bearer");
        Carrinho carrinho = carrinhos.ativo(tokenCarrinho);
        if (carrinho.getItens().isEmpty())
            throw new ConflitoException("O carrinho está vazio.");
        if (carrinho.getCliente() != null && !carrinho.getCliente().getId().equals(cliente.getId()))
            throw new ConflitoException("Este carrinho é de outro cliente.");
        boolean reservado = carrinho.getItens().stream().allMatch(i -> i.getReservas().stream()
                .anyMatch(r -> r.getStatus() == StatusReserva.ATIVA && r.getExpiraEm().isAfter(LocalDateTime.now())));
        if (!reservado)
            throw new ConflitoException("Os itens não estão reservados; volte ao carrinho e entre no checkout de novo.");

        EnderecoCliente endereco = dto.idEndereco() == null ? null : enderecos.doPerfil(dto.idEndereco(), cliente);
        if (endereco == null)
            throw ValidationException.of("idEndereco", "Endereço não encontrado (back)");
        MetodoPagamento metodo = MetodoPagamento.valueOf(dto.idMetodoPagamento());
        if (metodo == null)
            throw ValidationException.of("idMetodoPagamento", "Método de pagamento inválido (back)");
        if (metodo == MetodoPagamento.FATURAMENTO_PJ && cliente.getTipoPessoa() != TipoPessoa.PJ)
            throw ValidationException.of("idMetodoPagamento", "Faturamento só para pessoa jurídica (back)");

        CotacaoFreteResponseDTO cotacao = cotacaoEscolhida(carrinho, endereco, dto);

        Pedido pedido = new Pedido();
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setCliente(cliente);
        pedido.setCarrinho(carrinho);
        pedido.setMetodoPagamento(metodo);
        pedido.setEnderecoEntrega(congelar(endereco, cliente));
        for (ItemCarrinho i : carrinho.getItens()) {
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(i.getProduto());
            item.setQuantidade(i.getQuantidade());
            item.setPrecoUnitarioCongelado(i.getProduto().getPreco());
            pedido.getItens().add(item);
        }

        Entrega entrega = new Entrega();
        entrega.setPedido(pedido);
        entrega.setModalidade(cotacao.modalidade());
        entrega.setOrigemCalculo(cotacao.origemCalculo());
        entrega.setTransportadora(cotacao.transportadora());
        entrega.setCodigoServicoTransportadora(cotacao.codigoServicoTransportadora());
        entrega.setCustoCongelado(cotacao.custo());
        entrega.setPrazoCongeladoDias(cotacao.prazoDias());
        pedido.setEntrega(entrega);

        descontosDePromocao(pedido);
        Cupom cupom = carrinho.getCupomAplicado();
        if (cupom != null) {
            BigDecimal valor = carrinhos.desconto(carrinho, idUsuario);
            DescontoPedido d = new DescontoPedido();
            d.setPedido(pedido);
            d.setOrigem(OrigemDesconto.CUPOM);
            d.setCupom(cupom);
            d.setDescricao("Cupom " + cupom.getCodigo());
            d.setValor(valor);
            pedido.getDescontos().add(d);
        }

        BigDecimal desconto = pedido.getDescontos().stream().map(DescontoPedido::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add).min(pedido.getValorProdutos());
        pedido.setValorDesconto(desconto.setScale(2, RoundingMode.HALF_UP));
        pedido.setValorFrete(cotacao.custo());
        pedido.setValorTotal(pedido.getValorProdutos().add(cotacao.custo()).subtract(desconto)
                .setScale(2, RoundingMode.HALF_UP));

        HistoricoStatusPedido h = new HistoricoStatusPedido();
        h.setPedido(pedido);
        h.setStatusNovo(StatusPedido.PENDENTE);
        h.setOrigem(OrigemRegistro.SISTEMA);
        h.setObservacao("Pedido criado no checkout");
        pedido.getHistorico().add(h);
        pedidos.persist(pedido);

        if (cupom != null) {
            UsoCupom uso = new UsoCupom();
            uso.setCupom(cupom);
            uso.setCliente(cliente);
            uso.setPedido(pedido);
            usosCupom.persist(uso);
        }
        carrinho.setStatus(StatusCarrinho.CONVERTIDO);
        return pedido;
    }

    // Cota de novo no servidor: o preço vem da cotação, nunca do corpo (RN07)
    private CotacaoFreteResponseDTO cotacaoEscolhida(Carrinho c, EnderecoCliente endereco, CheckoutRequestDTO dto) {
        ModalidadeEntrega modalidade = ModalidadeEntrega.valueOf(dto.idModalidadeEntrega());
        if (modalidade == null)
            throw ValidationException.of("idModalidadeEntrega", "Modalidade de entrega inválida (back)");
        Map<Produto, Integer> itens = new LinkedHashMap<>();
        for (ItemCarrinho i : c.getItens())
            itens.merge(i.getProduto(), i.getQuantidade(), Integer::sum);
        List<CotacaoFreteResponseDTO> cotacoes = frete.cotar(endereco.getEndereco().getCep(), itens);
        String servico = dto.codigoServicoTransportadora();
        return cotacoes.stream().filter(x -> x.modalidade() == modalidade)
                .filter(x -> servico == null || servico.isBlank() || Objects.equals(servico, x.codigoServicoTransportadora())
                        || x.origemCalculo() != br.com.droneshop.pedido.model.OrigemCalculoFrete.INTEGRACAO_AGREGADOR)
                .findFirst()
                .orElseThrow(() -> ValidationException.of("idModalidadeEntrega", "Modalidade sem cotação válida (back)"));
    }

    private static EnderecoPedido congelar(EnderecoCliente ec, PerfilCliente cliente) {
        Endereco e = ec.getEndereco();
        EnderecoPedido p = new EnderecoPedido();
        p.setDestinatario(cliente.getNome());
        p.setLogradouro(e.getLogradouro());
        p.setNumero(e.getNumero());
        p.setComplemento(e.getComplemento());
        p.setBairro(e.getBairro());
        p.setCidade(e.getMunicipio().getNome());
        p.setUf(e.getMunicipio().getEstado().getSigla());
        p.setCep(e.getCep());
        return p;
    }

    // Promoção de escopo PRODUTO vigente: um desconto por item, a melhor promoção do produto (RN13)
    private void descontosDePromocao(Pedido pedido) {
        LocalDateTime agora = LocalDateTime.now();
        List<Promocao> vigentes = promocoes.findBySituacao("VIGENTE", agora).list().stream()
                .filter(p -> p.getEscopo() == EscopoPromocao.PRODUTO).toList();
        for (ItemPedido item : pedido.getItens()) {
            Promocao melhor = null;
            BigDecimal maior = BigDecimal.ZERO;
            for (Promocao p : vigentes) {
                if (p.getProdutos().stream().noneMatch(x -> x.getId().equals(item.getProduto().getId())))
                    continue;
                BigDecimal v = descontoUnitario(p, item.getPrecoUnitarioCongelado());
                if (v.compareTo(maior) > 0) {
                    maior = v;
                    melhor = p;
                }
            }
            if (melhor == null)
                continue;
            DescontoPedido d = new DescontoPedido();
            d.setPedido(pedido);
            d.setOrigem(OrigemDesconto.PROMOCAO);
            d.setPromocao(melhor);
            d.setDescricao("Promoção " + melhor.getNome());
            d.setValor(maior.multiply(BigDecimal.valueOf(item.getQuantidade())).setScale(2, RoundingMode.HALF_UP));
            pedido.getDescontos().add(d);
        }
    }

    static BigDecimal descontoUnitario(Promocao p, BigDecimal preco) {
        if (p.getPercentualDesconto() != null)
            return preco.multiply(p.getPercentualDesconto()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (p.getValorDesconto() != null)
            return p.getValorDesconto().min(preco);
        return BigDecimal.ZERO;
    }
}

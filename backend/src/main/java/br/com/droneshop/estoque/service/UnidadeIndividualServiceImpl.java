package br.com.droneshop.estoque.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.repository.ConfiguracaoLojaRepository;
import br.com.droneshop.catalogo.model.Drone;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.estoque.dto.UnidadeIndividualRequestDTO;
import br.com.droneshop.estoque.model.StatusUnidade;
import br.com.droneshop.estoque.model.UnidadeIndividual;
import br.com.droneshop.estoque.repository.UnidadeIndividualRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.pedido.model.ItemPedido;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import br.com.droneshop.pedido.service.StatusPedidoAlterado;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** Unidade individual por número de série (RN12, RN14, #34) e os drones da conta (#88). */
@ApplicationScoped
public class UnidadeIndividualServiceImpl implements UnidadeIndividualService {

    @Inject
    UnidadeIndividualRepository unidades;

    @Inject
    ProdutoRepository produtos;

    @Inject
    PedidoRepository pedidos;

    @Inject
    ConfiguracaoLojaRepository configuracao;

    @Override
    @Transactional
    public UnidadeIndividual criar(UnidadeIndividualRequestDTO dto) {
        Produto p = produtos.findById(dto.idProduto());
        if (!(p instanceof Drone))
            throw ValidationException.of("idProduto", "Número de série é só para drone (back)");
        String serie = dto.numeroSerie().trim();
        if (unidades.porSerie(serie) != null)
            throw ValidationException.of("numeroSerie", "Número de série já cadastrado (back)");
        UnidadeIndividual u = new UnidadeIndividual();
        u.setProduto(p);
        u.setNumeroSerie(serie);
        u.setRegistroAnac(Textos.nuloSeVazio(dto.registroAnac()));
        u.setStatus(StatusUnidade.DISPONIVEL);
        unidades.persist(u);
        return u;
    }

    @Override
    @Transactional
    public void vincular(Long idPedido, Long idItem, List<Long> idsUnidades) {
        Pedido pedido = pedidos.findById(idPedido);
        if (pedido == null)
            throw new ResourceNotFoundException("Pedido não encontrado.");
        ItemPedido item = pedido.getItens().stream().filter(i -> i.getId().equals(idItem)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado no pedido."));
        if (pedido.getStatus() != StatusPedido.PAGO && pedido.getStatus() != StatusPedido.SEPARACAO)
            throw new ConflitoException("Séries só se vinculam a pedido pago ou em separação.");
        List<Long> ids = idsUnidades == null ? List.of() : idsUnidades;
        Set<Long> distintos = new HashSet<>(ids);
        if (distintos.size() != ids.size())
            throw ValidationException.of("idsUnidades", "Unidade repetida (back)");
        if (ids.size() != item.getQuantidade())
            throw ValidationException.of("idsUnidades",
                    "Informe " + item.getQuantidade() + " unidade(s) para este item (back)");
        List<UnidadeIndividual> lista = ids.stream().map(unidades::findById).toList();
        for (UnidadeIndividual u : lista) {
            if (u == null || !u.getProduto().getId().equals(item.getProduto().getId())
                    || u.getStatus() != StatusUnidade.DISPONIVEL)
                throw ValidationException.of("idsUnidades", "Unidade inexistente, de outro produto ou indisponível (back)");
        }
        // troca de séries: as anteriores do item voltam a DISPONIVEL
        unidades.doItem(item.getId()).forEach(u -> {
            u.setItemPedido(null);
            u.setStatus(StatusUnidade.DISPONIVEL);
            u.setValidadeGarantia(null);
        });
        LocalDate garantia = LocalDate.now().plusMonths(mesesGarantia(item.getProduto()));
        for (UnidadeIndividual u : lista) {
            u.setItemPedido(item);
            u.setStatus(StatusUnidade.VENDIDA);
            u.setValidadeGarantia(garantia);
        }
    }

    private int mesesGarantia(Produto p) {
        if (p.getGarantiaMeses() > 0)
            return p.getGarantiaMeses();
        ConfiguracaoLoja c = configuracao.buscarUnica();
        return c == null ? 12 : Math.max(1, c.getPrazoGarantiaDias() / 30);
    }

    // RN12: drone só sai com número de série em cada unidade
    void aoMudarStatus(@Observes StatusPedidoAlterado e) {
        if (e.novo() != StatusPedido.ENVIADO)
            return;
        for (ItemPedido i : e.pedido().getItens()) {
            if (i.getProduto() instanceof Drone && unidades.doItem(i.getId()).size() < i.getQuantidade())
                throw new ConflitoException("Informe o número de série de cada drone antes de enviar ("
                        + i.getProduto().getNome() + ").");
        }
    }

    @Override
    @Transactional
    public void registrarAnac(Long idUsuario, Long idUnidade, String registroAnac) {
        UnidadeIndividual u = unidades.doCliente(idUnidade, idUsuario);
        if (u == null)
            throw new ResourceNotFoundException("Drone não encontrado.");
        String r = Textos.nuloSeVazio(registroAnac);
        if (r != null && r.length() > 40)
            throw ValidationException.of("registroAnac", "Use até 40 caracteres (back)");
        u.setRegistroAnac(r);
    }
}

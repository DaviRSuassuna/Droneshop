package br.com.droneshop.devolucao.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.repository.ConfiguracaoLojaRepository;
import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.service.KitCalculo;
import br.com.droneshop.devolucao.dto.DevolucaoRequestDTO;
import br.com.droneshop.devolucao.model.Devolucao;
import br.com.droneshop.devolucao.model.HistoricoDevolucao;
import br.com.droneshop.devolucao.model.ItemDevolucao;
import br.com.droneshop.devolucao.model.ResolucaoDevolucao;
import br.com.droneshop.devolucao.model.StatusDevolucao;
import br.com.droneshop.devolucao.repository.DevolucaoRepository;
import br.com.droneshop.estoque.dto.MovimentoEstoqueRequestDTO;
import br.com.droneshop.estoque.model.TipoMovimento;
import br.com.droneshop.estoque.service.MovimentoEstoqueService;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import br.com.droneshop.model.OrigemRegistro;
import br.com.droneshop.pagamento.model.Pagamento;
import br.com.droneshop.pagamento.service.EstornoService;
import br.com.droneshop.pedido.model.HistoricoStatusPedido;
import br.com.droneshop.pedido.model.ItemPedido;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import br.com.droneshop.pedido.service.NotaFiscalService;
import br.com.droneshop.pedido.service.PedidoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** Fluxo de devolução e troca (RF30, RF31, RF32, RN09, D8, D30). */
@ApplicationScoped
public class DevolucaoServiceImpl implements DevolucaoService {

    @Inject
    DevolucaoRepository devolucoes;

    @Inject
    PedidoRepository pedidos;

    @Inject
    UsuarioRepository usuarios;

    @Inject
    ConfiguracaoLojaRepository configuracao;

    @Inject
    PedidoService pedidoService;

    @Inject
    MovimentoEstoqueService movimentos;

    @Inject
    EstornoService estornos;

    @Inject
    NotaFiscalService notas;

    /** Transições permitidas (D30). */
    static boolean pode(StatusDevolucao de, StatusDevolucao para, ResolucaoDevolucao resolucao) {
        return switch (de) {
            case SOLICITADA -> para == StatusDevolucao.EM_ANALISE;
            case EM_ANALISE -> para == StatusDevolucao.APROVADA || para == StatusDevolucao.RECUSADA;
            case APROVADA -> para == StatusDevolucao.POSTAGEM_REVERSA;
            case POSTAGEM_REVERSA -> para == StatusDevolucao.RECEBIDA;
            case RECEBIDA -> resolucao == ResolucaoDevolucao.REEMBOLSO ? para == StatusDevolucao.REEMBOLSADA
                    : para == StatusDevolucao.CONCLUIDA;
            case REEMBOLSADA -> para == StatusDevolucao.CONCLUIDA;
            case RECUSADA, CONCLUIDA -> false;
        };
    }

    @Override
    @Transactional
    public Devolucao solicitar(Long idUsuario, DevolucaoRequestDTO dto) {
        Pedido pedido = pedidos.doUsuario(dto.idPedido(), idUsuario);
        if (pedido == null)
            throw ValidationException.of("idPedido", "Pedido não encontrado (back)");
        if (pedido.getStatus() != StatusPedido.ENTREGUE)
            throw new ConflitoException("Só pedido entregue pode ser devolvido.");
        ResolucaoDevolucao resolucao = ResolucaoDevolucao.valueOf(dto.idResolucao());
        if (resolucao == null)
            throw ValidationException.of("idResolucao", "Resolução inválida (back)");
        LocalDateTime entregue = pedido.getHistorico().stream()
                .filter(h -> h.getStatusNovo() == StatusPedido.ENTREGUE).map(HistoricoStatusPedido::getDataCadastro)
                .max(LocalDateTime::compareTo).orElse(pedido.getDataCadastro());
        ConfiguracaoLoja conf = configuracao.buscarUnica();
        int prazo = conf == null ? 7 : conf.getPrazoArrependimentoDias();
        if (entregue.plusDays(prazo).isBefore(LocalDateTime.now()))
            throw ValidationException.of("idPedido", "Passou o prazo de " + prazo + " dias para devolver (back)");
        if (dto.itens() == null || dto.itens().isEmpty())
            throw ValidationException.of("itens", "Escolha os itens a devolver (back)");

        Devolucao d = new Devolucao();
        d.setPedido(pedido);
        d.setStatus(StatusDevolucao.SOLICITADA);
        d.setResolucao(resolucao);
        d.setMotivo(dto.motivo().trim());
        d.setDataSolicitacao(LocalDateTime.now());
        Map<Long, Integer> somado = new HashMap<>();
        for (DevolucaoRequestDTO.Item i : dto.itens()) {
            ItemPedido item = pedido.getItens().stream().filter(x -> x.getId().equals(i.idItemPedido())).findFirst()
                    .orElseThrow(() -> ValidationException.of("itens", "Item não pertence ao pedido (back)"));
            int total = somado.merge(item.getId(), i.quantidade(), Integer::sum);
            int jaDevolvido = devolucoes.quantidadeDevolvida(item.getId());
            if (i.quantidade() < 1 || total + jaDevolvido > item.getQuantidade())
                throw ValidationException.of("itens", "Quantidade acima da comprada (back)");
            ItemDevolucao id = new ItemDevolucao();
            id.setDevolucao(d);
            id.setItemPedido(item);
            id.setQuantidade(i.quantidade());
            d.getItens().add(id);
        }
        historico(d, null, StatusDevolucao.SOLICITADA, usuarios.findById(idUsuario), "Solicitação do cliente");
        devolucoes.persist(d);
        return d;
    }

    private static void historico(Devolucao d, StatusDevolucao de, StatusDevolucao para, Usuario autor, String obs) {
        HistoricoDevolucao h = new HistoricoDevolucao();
        h.setDevolucao(d);
        h.setStatusAnterior(de);
        h.setStatusNovo(para);
        h.setOrigem(OrigemRegistro.MANUAL);
        h.setAutor(autor);
        h.setObservacao(obs == null || obs.isBlank() ? null : obs.trim());
        d.getHistorico().add(h);
    }

    @Override
    @Transactional
    public void mudarStatus(Long id, Long idStatus, String observacao, Long idAutor) {
        Devolucao d = buscar(id);
        StatusDevolucao novo = StatusDevolucao.valueOf(idStatus);
        if (novo == null)
            throw ValidationException.of("idStatus", "Status de devolução inexistente (back)");
        StatusDevolucao atual = d.getStatus();
        if (!pode(atual, novo, d.getResolucao()))
            throw new ConflitoException("A devolução não pode passar de " + atual.getNome() + " para " + novo.getNome() + ".");
        Pedido pedido = d.getPedido();
        switch (novo) {
            case APROVADA -> pedidoService.mudarSemAutor(pedido, StatusPedido.EM_DEVOLUCAO, OrigemRegistro.SISTEMA,
                    "Devolução aprovada");
            case RECEBIDA -> {
                receber(d);
                notas.emitirDevolucao(pedido, d.getId());
            }
            case REEMBOLSADA -> reembolsar(d);
            case CONCLUIDA -> {
                d.setDataConclusao(LocalDateTime.now());
                if (pedido.getStatus() == StatusPedido.EM_DEVOLUCAO)
                    pedidoService.mudarSemAutor(pedido,
                            d.getResolucao() == ResolucaoDevolucao.REEMBOLSO ? StatusPedido.DEVOLVIDO
                                    : StatusPedido.ENTREGUE,
                            OrigemRegistro.SISTEMA, "Devolução concluída");
            }
            default -> {
            }
        }
        d.setStatus(novo);
        historico(d, atual, novo, usuarios.findById(idAutor), observacao);
    }

    // Volta ao estoque por DEVOLUCAO MANUAL do usuário do token; kit volta por componente
    private void receber(Devolucao d) {
        for (ItemDevolucao i : d.getItens()) {
            var produto = i.getItemPedido().getProduto();
            if (produto instanceof Kit kit) {
                for (KitCalculo.Parte p : KitCalculo.partes(kit))
                    movimentos.registrar(new MovimentoEstoqueRequestDTO(p.produto().getId(),
                            TipoMovimento.DEVOLUCAO.getId(), p.quantidade() * i.getQuantidade(),
                            "Devolução " + d.getId()));
            } else {
                movimentos.registrar(new MovimentoEstoqueRequestDTO(produto.getId(), TipoMovimento.DEVOLUCAO.getId(),
                        i.getQuantidade(), "Devolução " + d.getId()));
            }
        }
    }

    // Estorno do valor dos itens no pagamento vigente (#36)
    private void reembolsar(Devolucao d) {
        Pagamento vigente = d.getPedido().getPagamentos().stream().filter(Pagamento::isVigente).findFirst()
                .orElseThrow(() -> new ConflitoException("O pedido não tem pagamento para estornar."));
        BigDecimal valor = d.getItens().stream()
                .map(i -> i.getItemPedido().getPrecoUnitarioCongelado().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        estornos.estornar(vigente.getId(), valor.min(vigente.getValor()), "Devolução " + d.getId());
    }

    @Override
    public Devolucao buscar(Long id) {
        Devolucao d = devolucoes.findById(id);
        if (d == null)
            throw new ResourceNotFoundException("Devolução não encontrada.");
        return d;
    }

    @Override
    public Devolucao doCliente(Long id, Long idUsuario) {
        Devolucao d = devolucoes.doUsuario(id, idUsuario);
        if (d == null)
            throw new ResourceNotFoundException("Devolução não encontrada.");
        return d;
    }

    @Override
    public List<Devolucao> doCliente(Long idUsuario) {
        return devolucoes.doUsuario(idUsuario);
    }
}

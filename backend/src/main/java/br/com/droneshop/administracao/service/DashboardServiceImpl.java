package br.com.droneshop.administracao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.droneshop.administracao.dto.DashboardResponseDTO;
import br.com.droneshop.estoque.dto.EstoqueResponseDTO;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
import br.com.droneshop.estoque.mapper.EstoqueResponseMapper;
import br.com.droneshop.estoque.repository.EstoqueRepository;
import br.com.droneshop.pedido.dto.PedidoFiltro;
import br.com.droneshop.pedido.dto.PedidoListagemResponseDTO;
import br.com.droneshop.pedido.dto.ResumoStatusPedidoResponseDTO;
import br.com.droneshop.pedido.mapper.PedidoResponseMapper;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import br.com.droneshop.pedido.service.PedidoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DashboardServiceImpl implements DashboardService {

    // entram no faturamento os pedidos pagos que não foram cancelados nem devolvidos
    private static final Set<StatusPedido> FATURADOS = EnumSet.of(
            StatusPedido.PAGO, StatusPedido.SEPARACAO, StatusPedido.ENVIADO, StatusPedido.ENTREGUE);
    private static final Set<StatusPedido> AGUARDANDO = EnumSet.of(
            StatusPedido.PENDENTE, StatusPedido.PAGO, StatusPedido.SEPARACAO);
    private static final int DIAS_PADRAO = 30;
    private static final int ULTIMOS_PEDIDOS = 5;

    @Inject
    PedidoService pedidoService;

    @Inject
    PedidoRepository pedidoRepository;

    @Inject
    EstoqueRepository estoqueRepository;

    @Inject
    EntityManager em;

    @Override
    @Transactional
    public DashboardResponseDTO resumo(String dataInicio, String dataFim) {
        PedidoFiltro filtro = periodo(dataInicio, dataFim);
        LocalDateTime inicio = filtro.inicio();
        LocalDateTime fim = filtro.fimExclusivo();

        PedidoRepository.Totais totais = pedidoRepository.totalizar(FATURADOS, inicio, fim);
        BigDecimal faturamento = totais.valorTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal ticketMedio = totais.quantidade() == 0
                ? BigDecimal.ZERO.setScale(2)
                : faturamento.divide(BigDecimal.valueOf(totais.quantidade()), 2, RoundingMode.HALF_UP);

        Map<StatusPedido, Long> contagem = pedidoRepository.contarPorStatus(inicio, fim);
        List<ResumoStatusPedidoResponseDTO> porStatus = Arrays.stream(StatusPedido.values())
                .map(s -> new ResumoStatusPedidoResponseDTO(s, contagem.getOrDefault(s, 0L)))
                .toList();

        // mesma regra do GET /estoque?situacao=ABAIXO_MINIMO (#13): kit fica de fora
        List<DashboardResponseDTO.EstoqueBaixo> estoqueBaixo = estoqueRepository
                .findComFiltro(SituacaoEstoque.ABAIXO_MINIMO, null, null).list().stream()
                .map(EstoqueResponseMapper::toEstoque)
                .map(DashboardServiceImpl::estoqueBaixo)
                .toList();

        List<PedidoListagemResponseDTO> ultimos = pedidoService.findComFiltro(filtro, 0, ULTIMOS_PEDIDOS).stream()
                .map(PedidoResponseMapper::toListagem)
                .toList();

        // período anterior de mesmo tamanho, terminando na véspera de dataInicio
        long dias = ChronoUnit.DAYS.between(inicio, fim);
        LocalDateTime inicioAnterior = inicio.minusDays(dias);
        PedidoRepository.Totais anterior = pedidoRepository.totalizar(FATURADOS, inicioAnterior, inicio);
        BigDecimal fatAnterior = anterior.valorTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal ticketAnterior = anterior.quantidade() == 0
                ? BigDecimal.ZERO.setScale(2)
                : fatAnterior.divide(BigDecimal.valueOf(anterior.quantidade()), 2, RoundingMode.HALF_UP);
        DashboardResponseDTO.Comparacao comparacao = new DashboardResponseDTO.Comparacao(
                new DashboardResponseDTO.Periodo(inicioAnterior.toLocalDate(), inicio.toLocalDate().minusDays(1)),
                fatAnterior, anterior.quantidade(), ticketAnterior,
                variacao(faturamento, fatAnterior),
                variacao(BigDecimal.valueOf(totais.quantidade()), BigDecimal.valueOf(anterior.quantidade())),
                variacao(ticketMedio, ticketAnterior));

        List<Pedido> aguardando = em.createQuery("select p from Pedido p where p.status in :s"
                + " order by p.dataCadastro, p.id", Pedido.class).setParameter("s", AGUARDANDO)
                .setMaxResults(ULTIMOS_PEDIDOS).getResultList();
        long qtdAguardando = em.createQuery("select count(p) from Pedido p where p.status in :s", Long.class)
                .setParameter("s", AGUARDANDO).getSingleResult();

        return new DashboardResponseDTO(
                new DashboardResponseDTO.Periodo(inicio.toLocalDate(), fim.toLocalDate().minusDays(1)),
                faturamento,
                totais.quantidade(),
                ticketMedio,
                porStatus,
                estoqueBaixo,
                ultimos,
                comparacao,
                vendasPorDia(inicio, fim),
                qtdAguardando,
                aguardando.stream().map(PedidoResponseMapper::toListagem).toList(),
                maisVendidos(inicio, fim, faturamento),
                receitaPorCategoria(inicio, fim));
    }

    @Override
    @Transactional
    public List<DashboardResponseDTO.VendaDia> vendasPorDia(String dataInicio, String dataFim) {
        PedidoFiltro filtro = periodo(dataInicio, dataFim);
        return vendasPorDia(filtro.inicio(), filtro.fimExclusivo());
    }

    private static BigDecimal variacao(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.signum() == 0)
            return null;
        return atual.subtract(anterior).multiply(BigDecimal.valueOf(100))
                .divide(anterior, 1, RoundingMode.HALF_UP);
    }

    // um ponto por dia, inclusive sem venda; o dia anterior correspondente fica "dias" antes
    private List<DashboardResponseDTO.VendaDia> vendasPorDia(LocalDateTime inicio, LocalDateTime fim) {
        long dias = ChronoUnit.DAYS.between(inicio, fim);
        LocalDateTime inicioAnterior = inicio.minusDays(dias);
        Map<LocalDate, BigDecimal> fat = new HashMap<>();
        Map<LocalDate, Long> qtd = new HashMap<>();
        for (Object[] l : em.createQuery("select p.dataCadastro, p.valorTotal from Pedido p where p.status in :s"
                + " and p.dataCadastro >= :i and p.dataCadastro < :f", Object[].class)
                .setParameter("s", FATURADOS).setParameter("i", inicioAnterior).setParameter("f", fim)
                .getResultList()) {
            LocalDate d = ((LocalDateTime) l[0]).toLocalDate();
            fat.merge(d, l[1] == null ? BigDecimal.ZERO : (BigDecimal) l[1], BigDecimal::add);
            qtd.merge(d, 1L, Long::sum);
        }
        List<DashboardResponseDTO.VendaDia> r = new ArrayList<>();
        for (LocalDate d = inicio.toLocalDate(); d.isBefore(fim.toLocalDate()); d = d.plusDays(1)) {
            LocalDate a = d.minusDays(dias);
            r.add(new DashboardResponseDTO.VendaDia(d,
                    fat.getOrDefault(d, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP), qtd.getOrDefault(d, 0L),
                    fat.getOrDefault(a, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP), qtd.getOrDefault(a, 0L)));
        }
        return r;
    }

    private static BigDecimal participacao(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0)
            return BigDecimal.ZERO.setScale(1);
        BigDecimal p = parte.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
        return p.min(BigDecimal.valueOf(100).setScale(1));
    }

    // receita = quantidade x preço congelado do item (RN06)
    private List<DashboardResponseDTO.MaisVendido> maisVendidos(LocalDateTime inicio, LocalDateTime fim,
            BigDecimal faturamento) {
        return em.createQuery("select i.produto.id, i.produto.nome, i.produto.sku, sum(i.quantidade),"
                + " sum(i.quantidade * i.precoUnitarioCongelado) from ItemPedido i where i.pedido.status in :s"
                + " and i.pedido.dataCadastro >= :i and i.pedido.dataCadastro < :f"
                + " group by i.produto.id, i.produto.nome, i.produto.sku"
                + " order by sum(i.quantidade * i.precoUnitarioCongelado) desc", Object[].class)
                .setParameter("s", FATURADOS).setParameter("i", inicio).setParameter("f", fim)
                .setMaxResults(5).getResultList().stream()
                .map(l -> {
                    BigDecimal receita = ((BigDecimal) l[4]).setScale(2, RoundingMode.HALF_UP);
                    return new DashboardResponseDTO.MaisVendido(
                            new DashboardResponseDTO.ProdutoVendido((Long) l[0], (String) l[1], (String) l[2]),
                            ((Number) l[3]).longValue(), receita, participacao(receita, faturamento));
                }).toList();
    }

    private List<DashboardResponseDTO.ReceitaCategoria> receitaPorCategoria(LocalDateTime inicio,
            LocalDateTime fim) {
        List<Object[]> linhas = em.createQuery("select c.nome,"
                + " sum(i.quantidade * i.precoUnitarioCongelado) from ItemPedido i join i.produto pr"
                + " left join pr.subcategoria s left join s.categoria c where i.pedido.status in :s"
                + " and i.pedido.dataCadastro >= :i and i.pedido.dataCadastro < :f"
                + " group by c.nome"
                + " order by sum(i.quantidade * i.precoUnitarioCongelado) desc", Object[].class)
                .setParameter("s", FATURADOS).setParameter("i", inicio).setParameter("f", fim).getResultList();
        BigDecimal total = linhas.stream().map(l -> (BigDecimal) l[1]).reduce(BigDecimal.ZERO, BigDecimal::add);
        return linhas.stream().map(l -> new DashboardResponseDTO.ReceitaCategoria(
                l[0] == null ? "Sem categoria" : (String) l[0],
                ((BigDecimal) l[1]).setScale(2, RoundingMode.HALF_UP), participacao((BigDecimal) l[1], total)))
                .toList();
    }

    // Valida como o GET /pedidos e completa o que faltar: sem dataFim, hoje; sem dataInicio,
    // 30 dias antes do fim. A segunda validação barra o período invertido pela data padrão.
    private PedidoFiltro periodo(String dataInicio, String dataFim) {
        PedidoFiltro informado = pedidoService.filtro(null, dataInicio, dataFim, null);
        LocalDate fim = informado.fimExclusivo() == null
                ? LocalDate.now()
                : informado.fimExclusivo().toLocalDate().minusDays(1);
        LocalDate inicio = informado.inicio() == null ? fim.minusDays(DIAS_PADRAO) : informado.inicio().toLocalDate();
        return pedidoService.filtro(null, inicio.toString(), fim.toString(), null);
    }

    private static DashboardResponseDTO.EstoqueBaixo estoqueBaixo(EstoqueResponseDTO e) {
        return new DashboardResponseDTO.EstoqueBaixo(e.produto(), e.disponivel(), e.estoqueMinimo());
    }
}

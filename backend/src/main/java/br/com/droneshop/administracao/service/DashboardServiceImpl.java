package br.com.droneshop.administracao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import br.com.droneshop.pedido.service.PedidoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DashboardServiceImpl implements DashboardService {

    // entram no faturamento os pedidos pagos que não foram cancelados nem devolvidos
    private static final Set<StatusPedido> FATURADOS = EnumSet.of(
            StatusPedido.PAGO, StatusPedido.SEPARACAO, StatusPedido.ENVIADO, StatusPedido.ENTREGUE);
    private static final int DIAS_PADRAO = 30;
    private static final int ULTIMOS_PEDIDOS = 5;

    @Inject
    PedidoService pedidoService;

    @Inject
    PedidoRepository pedidoRepository;

    @Inject
    EstoqueRepository estoqueRepository;

    @Override
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

        return new DashboardResponseDTO(
                new DashboardResponseDTO.Periodo(inicio.toLocalDate(), fim.toLocalDate().minusDays(1)),
                faturamento,
                totais.quantidade(),
                ticketMedio,
                porStatus,
                estoqueBaixo,
                ultimos);
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

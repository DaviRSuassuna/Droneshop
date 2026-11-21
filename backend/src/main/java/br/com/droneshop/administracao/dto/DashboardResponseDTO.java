package br.com.droneshop.administracao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.droneshop.estoque.dto.EstoqueResponseDTO;
import br.com.droneshop.pedido.dto.PedidoListagemResponseDTO;
import br.com.droneshop.pedido.dto.ResumoStatusPedidoResponseDTO;

// GET /admin/dashboard. Os números de pedido são do período (dataCadastro, datas inclusivas);
// estoqueBaixo é o saldo de agora, sem período.
public record DashboardResponseDTO(
    Periodo periodo,
    BigDecimal faturamento,
    long quantidadePedidos,
    BigDecimal ticketMedio,
    List<ResumoStatusPedidoResponseDTO> pedidosPorStatus,
    List<EstoqueBaixo> estoqueBaixo,
    List<PedidoListagemResponseDTO> ultimosPedidos,
    Comparacao comparacao,
    List<VendaDia> vendasPorDia,
    long quantidadeAguardandoAcao,
    List<PedidoListagemResponseDTO> pedidosAguardandoAcao,
    List<MaisVendido> maisVendidos,
    List<ReceitaCategoria> receitaPorCategoria
) {
    /** Período anterior de mesmo tamanho, terminando na véspera de dataInicio; variação nula sem base. */
    public record Comparacao(Periodo periodoAnterior, BigDecimal faturamento, long quantidadePedidos,
            BigDecimal ticketMedio, BigDecimal variacaoFaturamento, BigDecimal variacaoPedidos,
            BigDecimal variacaoTicketMedio) {
    }

    public record VendaDia(LocalDate data, BigDecimal faturamento, long quantidadePedidos,
            BigDecimal faturamentoAnterior, long quantidadePedidosAnterior) {
    }

    public record ProdutoVendido(Long id, String nome, String sku) {
    }

    public record MaisVendido(ProdutoVendido produto, long unidades, BigDecimal receita, BigDecimal participacao) {
    }

    public record ReceitaCategoria(String categoria, BigDecimal receita, BigDecimal participacao) {
    }

    public record Periodo(LocalDate dataInicio, LocalDate dataFim) {
    }

    public record EstoqueBaixo(
        EstoqueResponseDTO.Produto produto,
        int disponivel,
        int estoqueMinimo
    ) {
    }
}

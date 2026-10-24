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
    List<PedidoListagemResponseDTO> ultimosPedidos
) {
    public record Periodo(LocalDate dataInicio, LocalDate dataFim) {
    }

    public record EstoqueBaixo(
        EstoqueResponseDTO.Produto produto,
        int disponivel,
        int estoqueMinimo
    ) {
    }
}

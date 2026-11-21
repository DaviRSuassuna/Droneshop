package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;

// GET /pedidos/indicadores (#89): números de agora, como /pedidos/resumo-status
public record IndicadoresPedidoResponseDTO(
    long aSeparar,
    AguardandoPagamento aguardandoPagamento,
    EmTransito emTransito,
    long devolucoesAbertas
) {
    public record AguardandoPagamento(long quantidade, BigDecimal valorEmAberto) {
    }

    // prazoMedioDias: média ENVIADO -> ENTREGUE dos entregues nos últimos 30 dias; null sem entregas
    public record EmTransito(long quantidade, Double prazoMedioDias) {
    }
}

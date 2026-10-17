package br.com.droneshop.pedido.dto;

import java.time.LocalDateTime;

import br.com.droneshop.pedido.model.StatusPedido;

// Filtros de GET /pedidos já validados. Período: [inicio, fimExclusivo). idBusca vem do número do pedido.
public record PedidoFiltro(
    StatusPedido status,
    LocalDateTime inicio,
    LocalDateTime fimExclusivo,
    String nomeCliente,
    Long idBusca
) {
}

package br.com.droneshop.pedido.dto;

import br.com.droneshop.pedido.model.StatusPedido;

public record ResumoStatusPedidoResponseDTO(
    StatusPedido status,
    long quantidade
) {
}

package br.com.droneshop.pedido.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

// POST /pedidos/etiquetas (#89): um PDF com uma página por pedido, na ordem dos ids
public record EtiquetasRequestDTO(
    @NotEmpty(message = "Informe ao menos um pedido. (back)")
    List<Long> ids
) {
}

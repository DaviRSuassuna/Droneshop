package br.com.droneshop.compra.dto;

import jakarta.validation.constraints.NotNull;

public record ItemListaDesejosRequestDTO(@NotNull(message = "Informe o produto (back)") Long idProduto) {
}

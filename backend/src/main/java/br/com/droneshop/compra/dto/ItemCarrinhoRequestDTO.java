package br.com.droneshop.compra.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCarrinhoRequestDTO(
        @NotNull(message = "Informe o produto (back)") Long idProduto,
        @NotNull(message = "Informe a quantidade (back)") @Min(value = 1, message = "A quantidade mínima é 1 (back)") Integer quantidade) {
}

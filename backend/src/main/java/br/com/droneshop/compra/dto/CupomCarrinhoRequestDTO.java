package br.com.droneshop.compra.dto;

import jakarta.validation.constraints.NotBlank;

public record CupomCarrinhoRequestDTO(@NotBlank(message = "Informe o código do cupom (back)") String codigo) {
}

package br.com.droneshop.pagamento.dto;

import jakarta.validation.constraints.NotNull;

public record PagamentoRequestDTO(
        @NotNull(message = "Escolha o método de pagamento (back)") Long idMetodo,
        String tokenCartao,
        Long idCartaoSalvo,
        Boolean salvarCartao,
        String ultimosDigitos,
        String bandeira,
        String validade) {
}

package br.com.droneshop.pagamento.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * numero só existe para recusar o corpo que traga o número completo (RN11).
 * Formato dos últimos dígitos e da validade: conferidos no CartaoSalvoService.
 */
public record CartaoSalvoRequestDTO(
        @NotBlank(message = "Informe o token do cartão (back)") String tokenGateway,
        String ultimosDigitos,
        String bandeira,
        String validade,
        String numero) {
}

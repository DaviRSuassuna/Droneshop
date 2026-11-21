package br.com.droneshop.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodigoRastreioRequestDTO(
        @NotBlank(message = "Informe o código de rastreio (back)") @Size(max = 60, message = "Use até 60 caracteres (back)") String codigoRastreio) {
}

package br.com.droneshop.pedido.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EventoRastreioRequestDTO(
        @NotBlank(message = "Informe a descrição (back)") @Size(max = 200, message = "Use até 200 caracteres (back)") String descricao,
        @Size(max = 120, message = "Use até 120 caracteres (back)") String localizacao,
        LocalDateTime dataHora) {
}

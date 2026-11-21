package br.com.droneshop.identidade.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsentimentoRequestDTO(
        @NotBlank(message = "Informe o tipo (back)") String tipo,
        @NotBlank(message = "Informe a versão (back)") String versao) {
}

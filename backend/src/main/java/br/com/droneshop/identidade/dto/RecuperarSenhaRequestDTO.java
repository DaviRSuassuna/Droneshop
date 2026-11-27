package br.com.droneshop.identidade.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarSenhaRequestDTO(
        @NotBlank(message = "E-mail é obrigatório (back).")
        @Email(message = "E-mail inválido (back).") String email) {
}

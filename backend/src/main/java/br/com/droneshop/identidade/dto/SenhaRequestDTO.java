package br.com.droneshop.identidade.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

public record SenhaRequestDTO(
    @NotBlank(message = "O campo deve ser informado (back).")
    String senhaAtual,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres (back).")
    String senhaNova
) {
}

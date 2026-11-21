package br.com.droneshop.identidade.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

// Documento e tipo de pessoa não mudam depois da criação da conta.
public record PerfilRequestDTO(
    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 120, message = "O campo deve ter no máximo 120 caracteres (back).")
    String nome,

    @Length(max = 20, message = "O campo deve ter no máximo 20 caracteres (back).")
    String telefone
) {
}

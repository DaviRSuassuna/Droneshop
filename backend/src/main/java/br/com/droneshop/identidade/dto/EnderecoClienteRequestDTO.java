package br.com.droneshop.identidade.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Campos do endereço achatados, como no contrato; município pelo id (D32).
public record EnderecoClienteRequestDTO(
    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    String apelido,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido. Use o formato 00000-000 (back).")
    String cep,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 150, message = "O campo deve ter no máximo 150 caracteres (back).")
    String logradouro,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 10, message = "O campo deve ter no máximo 10 caracteres (back).")
    String numero,

    @Length(max = 60, message = "O campo deve ter no máximo 60 caracteres (back).")
    String complemento,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 80, message = "O campo deve ter no máximo 80 caracteres (back).")
    String bairro,

    @NotNull(message = "Selecione o município (back).")
    Long idMunicipio,

    boolean principal
) {
}

package br.com.droneshop.estoque.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

// O minimo da quantidade depende do tipo (AJUSTE aceita 0), entao o service confere.
// Pela API o movimento e sempre MANUAL, com o usuario do token como autor.
public record MovimentoEstoqueRequestDTO(
    @NotNull(message = "Selecione a variante (back).")
    Long idVariante,

    @NotNull(message = "Selecione o tipo do movimento (back).")
    Long idTipo,

    @NotNull(message = "Informe a quantidade (back).")
    Integer quantidade,

    @Length(max = 500, message = "O campo deve ter no máximo 500 caracteres (back).")
    String observacao
) {
}

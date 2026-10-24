package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubcategoriaRequestDTO(
    @Length(min = 2, max = 100, message = "O campo deve conter entre 2 e 100 caracteres (back).")
    String nome,

    @Length(max = 110, message = "O campo deve ter no máximo 110 caracteres (back).")
    String slug,

    Long idCategoria,

    @PositiveOrZero(message = "O campo deve ser maior ou igual a zero (back).")
    Integer ordemExibicao,

    @Length(max = 2000, message = "O campo deve ter no máximo 2000 caracteres (back).")
    String descricao,

    @NotNull(message = "O status deve ser informado (back).")
    Long idStatus
) {
    public SubcategoriaRequestDTO {
        nome = br.com.droneshop.util.Textos.nuloSeVazio(nome);
        slug = br.com.droneshop.util.Textos.nuloSeVazio(slug);
        descricao = br.com.droneshop.util.Textos.nuloSeVazio(descricao);
    }
}

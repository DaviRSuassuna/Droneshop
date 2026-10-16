package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

// nome, ordemExibicao e idTipo so sao obrigatorios fora de RASCUNHO (CategoriaServiceImpl).
// Texto em branco vira null no construtor, como em MarcaRequestDTO.
public record CategoriaRequestDTO(
    @Length(min = 2, max = 100, message = "O campo deve conter entre 2 e 100 caracteres (back).")
    String nome,

    @Length(max = 110, message = "O campo deve ter no máximo 110 caracteres (back).")
    String slug,

    @PositiveOrZero(message = "O campo deve ser maior ou igual a zero (back).")
    Integer ordemExibicao,

    @Length(max = 2000, message = "O campo deve ter no máximo 2000 caracteres (back).")
    String descricao,

    @NotNull(message = "O status deve ser informado (back).")
    Long idStatus,

    Long idTipo,

    Boolean exibirMenu,

    @Length(max = 255, message = "O campo deve ter no máximo 255 caracteres (back).")
    String iconeUrl
) {
    public CategoriaRequestDTO {
        nome = Textos.nuloSeVazio(nome);
        slug = Textos.nuloSeVazio(slug);
        descricao = Textos.nuloSeVazio(descricao);
        iconeUrl = Textos.nuloSeVazio(iconeUrl);
    }
}

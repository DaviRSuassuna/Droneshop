package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public final class SubcategoriaDTO {

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 2, max = 100, message = "O campo deve conter entre 2 e 100 caracteres (back).")
    private final String nome;

    @Length(max = 110, message = "O campo deve ter no máximo 110 caracteres (back).")
    private final String slug;

    @NotNull(message = "Selecione a categoria (back).")
    private final Long categoriaId;

    @PositiveOrZero(message = "O campo deve ser maior ou igual a zero (back).")
    private final Integer ordemExibicao;

    @Length(max = 2000, message = "O campo deve ter no máximo 2000 caracteres (back).")
    private final String descricao;

    // valor esperado: "RASCUNHO", "ATIVA" ou "INATIVA"
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String status;

    public SubcategoriaDTO(String nome, String slug, Long categoriaId, Integer ordemExibicao,
                           String descricao, String status) {
        this.nome = nome;
        this.slug = slug;
        this.categoriaId = categoriaId;
        this.ordemExibicao = ordemExibicao;
        this.descricao = descricao;
        this.status = status;
    }

    public String getNome() {
        return nome;
    }

    public String getSlug() {
        return slug;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public Integer getOrdemExibicao() {
        return ordemExibicao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getStatus() {
        return status;
    }
}

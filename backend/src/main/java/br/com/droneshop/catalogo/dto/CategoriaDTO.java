package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public final class CategoriaDTO {

    // nome e ordemExibicao NAO tem @NotBlank/@NotNull aqui: obrigatoriedade
    // depende do status (RASCUNHO nao exige, ATIVA/INATIVA exige) e quem
    // valida isso e o CategoriaServiceImpl, nao o Bean Validation.

    @Length(min = 2, max = 100, message = "O campo deve conter entre 2 e 100 caracteres (back).")
    private final String nome;

    @Length(max = 110, message = "O campo deve ter no máximo 110 caracteres (back).")
    private final String slug;

    @PositiveOrZero(message = "O campo deve ser maior ou igual a zero (back).")
    private final Integer ordemExibicao;

    @Length(max = 2000, message = "O campo deve ter no máximo 2000 caracteres (back).")
    private final String descricao;

    // valor esperado: "RASCUNHO", "ATIVA" ou "INATIVA"
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String status;

    private final Boolean exibirMenu;

    @Length(max = 255, message = "O campo deve ter no máximo 255 caracteres (back).")
    private final String iconeUrl;

    public CategoriaDTO(String nome, String slug, Integer ordemExibicao,
                        String descricao, String status, Boolean exibirMenu, String iconeUrl) {
        this.nome = nome;
        this.slug = slug;
        this.ordemExibicao = ordemExibicao;
        this.descricao = descricao;
        this.status = status;
        this.exibirMenu = exibirMenu;
        this.iconeUrl = iconeUrl;
    }

    public String getNome() {
        return nome;
    }

    public String getSlug() {
        return slug;
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

    public Boolean getExibirMenu() {
        return exibirMenu;
    }

    public String getIconeUrl() {
        return iconeUrl;
    }
}

package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;

public final class CategoriaDTO {
    @Length(min = 2, max = 100)
    private final String nome;
    @Length(max = 110)
    private final String slug;
    private final Long categoriaPaiId;
    private final Integer ordemExibicao;
    @Length(max = 2000)
    private final String descricao;
    @NotBlank
    private final String status;
    private final Boolean exibirMenu;
    @Length(max = 255)
    private final String iconeUrl;

    public CategoriaDTO(String nome, String slug, Long categoriaPaiId, Integer ordemExibicao,
                        String descricao, String status, Boolean exibirMenu, String iconeUrl) {
        this.nome = nome; this.slug = slug; this.categoriaPaiId = categoriaPaiId;
        this.ordemExibicao = ordemExibicao; this.descricao = descricao; this.status = status;
        this.exibirMenu = exibirMenu; this.iconeUrl = iconeUrl;
    }
    public String getNome() { return nome; }
    public String getSlug() { return slug; }
    public Long getCategoriaPaiId() { return categoriaPaiId; }
    public Integer getOrdemExibicao() { return ordemExibicao; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }
    public Boolean getExibirMenu() { return exibirMenu; }
    public String getIconeUrl() { return iconeUrl; }
}

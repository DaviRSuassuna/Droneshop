package br.com.droneshop.catalogo.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Categoria extends DefaultEntity {

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 110, nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, name = "ordem_exibicao")
    private Integer ordemExibicao;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StatusCategoria status;

    @Column(nullable = false, name = "exibir_menu")
    private boolean exibirMenu;

    @Column(length = 255, name = "icone_url")
    private String iconeUrl;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getOrdemExibicao() {
        return ordemExibicao;
    }

    public void setOrdemExibicao(Integer ordemExibicao) {
        this.ordemExibicao = ordemExibicao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusCategoria getStatus() {
        return status;
    }

    public void setStatus(StatusCategoria status) {
        this.status = status;
    }

    public boolean isExibirMenu() {
        return exibirMenu;
    }

    public void setExibirMenu(boolean exibirMenu) {
        this.exibirMenu = exibirMenu;
    }

    public String getIconeUrl() {
        return iconeUrl;
    }

    public void setIconeUrl(String iconeUrl) {
        this.iconeUrl = iconeUrl;
    }

}

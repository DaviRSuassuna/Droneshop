package br.com.droneshop.catalogo.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Marca extends DefaultEntity {

    @Column(length = 80, nullable = false)
    private String nome;

    @Column(length = 90, nullable = false, unique = true)
    private String slug;

    @Column(length = 60, nullable = false, name = "pais_origem")
    private String paisOrigem;

    @Column(length = 255, name = "site_oficial")
    private String siteOficial;

    @Column(length = 2000)
    private String descricao;

    @Column(length = 255, nullable = false, name = "logo_url")
    private String logoUrl;

    @Column(nullable = false)
    private StatusMarca status;

    @Column(nullable = false, name = "destacar_home")
    private boolean destacarHome;

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

    public String getPaisOrigem() {
        return paisOrigem;
    }

    public void setPaisOrigem(String paisOrigem) {
        this.paisOrigem = paisOrigem;
    }

    public String getSiteOficial() {
        return siteOficial;
    }

    public void setSiteOficial(String siteOficial) {
        this.siteOficial = siteOficial;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public StatusMarca getStatus() {
        return status;
    }

    public void setStatus(StatusMarca status) {
        this.status = status;
    }

    public boolean isDestacarHome() {
        return destacarHome;
    }

    public void setDestacarHome(boolean destacarHome) {
        this.destacarHome = destacarHome;
    }

}

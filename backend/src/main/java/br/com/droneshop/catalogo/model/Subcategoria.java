package br.com.droneshop.catalogo.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Subcategoria extends DefaultEntity {

    // Contagem derivada, sem coluna ou saldo mantido pelo cadastro.
    @org.hibernate.annotations.Formula("(SELECT count(*) FROM produto p WHERE p.id_subcategoria = id)")
    private long quantidadeProdutos;

    public long getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 110, nullable = false, unique = true)
    private String slug;

    // Rascunho pode ficar sem categoria; publicar exige o vínculo (D21).
    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Column(nullable = false, name = "ordem_exibicao")
    private Integer ordemExibicao;

    @Column(length = 2000)
    private String descricao;

    // mesmo ciclo de vida da Categoria (RASCUNHO, ATIVA, INATIVA)
    @Column(nullable = false)
    private StatusCategoria status;

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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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

}

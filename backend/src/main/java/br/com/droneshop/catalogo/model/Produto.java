package br.com.droneshop.catalogo.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Heranca JOINED (D27): produto guarda o comum, drone e acessorio guardam o proprio
// e compartilham o id. A coluna tipo guarda o id de TipoProduto. O preco fica aqui
// ate VarianteProduto existir (#9).
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Produto extends DefaultEntity {

    @Column(length = 120)
    private String nome;

    @Column(length = 40, unique = true)
    private String modelo;

    @Column(length = 300, name = "descricao_curta")
    private String descricaoCurta;

    @Column(length = 5000)
    private String descricao;

    @Column(length = 140, nullable = false, unique = true)
    private String slug;

    @Column(length = 70, name = "meta_title")
    private String metaTitle;

    @Column(length = 160, name = "meta_description")
    private String metaDescription;

    @Column(length = 255)
    private String tags;

    @Column(nullable = false, name = "garantia_meses")
    private int garantiaMeses;

    @Column(name = "data_lancamento")
    private LocalDate dataLancamento;

    @Column(precision = 12, scale = 2)
    private BigDecimal preco;

    // muitos produtos -> uma marca
    @ManyToOne
    @JoinColumn(name = "id_marca")
    private Marca marca;

    // muitos produtos -> uma subcategoria (a categoria vem por ela)
    @ManyToOne
    @JoinColumn(name = "id_subcategoria")
    private Subcategoria subcategoria;

    // muitos produtos -> um fornecedor (RF02)
    @ManyToOne
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor fornecedor;

    @Column(nullable = false)
    private StatusProduto status;

    @Column(nullable = false)
    private boolean destaque;

    public abstract TipoProduto getTipo();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getDescricaoCurta() {
        return descricaoCurta;
    }

    public void setDescricaoCurta(String descricaoCurta) {
        this.descricaoCurta = descricaoCurta;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getGarantiaMeses() {
        return garantiaMeses;
    }

    public void setGarantiaMeses(int garantiaMeses) {
        this.garantiaMeses = garantiaMeses;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(LocalDate dataLancamento) {
        this.dataLancamento = dataLancamento;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Subcategoria getSubcategoria() {
        return subcategoria;
    }

    public void setSubcategoria(Subcategoria subcategoria) {
        this.subcategoria = subcategoria;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public void setStatus(StatusProduto status) {
        this.status = status;
    }

    public boolean isDestaque() {
        return destaque;
    }

    public void setDestaque(boolean destaque) {
        this.destaque = destaque;
    }

}

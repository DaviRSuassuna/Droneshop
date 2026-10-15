package br.com.droneshop.catalogo.model;

import java.math.BigDecimal;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Versao inicial de Produto: ainda sem a heranca Drone/Acessorio e sem VarianteProduto
// do diagrama. O preco fica aqui ate VarianteProduto existir.
@Entity
public class Produto extends DefaultEntity {

    @Column(length = 120, nullable = false)
    private String nome;

    @Column(length = 40, nullable = false, unique = true)
    private String modelo;

    @Column(length = 300, name = "descricao_curta")
    private String descricaoCurta;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal preco;

    // muitos produtos -> uma marca
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_marca", nullable = false)
    private Marca marca;

    // muitos produtos -> uma subcategoria (a categoria vem por ela)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_subcategoria", nullable = false)
    private Subcategoria subcategoria;

    // muitos produtos -> um fornecedor (RF02)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_fornecedor", nullable = false)
    private Fornecedor fornecedor;

    @Column(nullable = false)
    private StatusProduto status;

    @Column(nullable = false)
    private boolean destaque;

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

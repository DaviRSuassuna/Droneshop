package br.com.droneshop.catalogo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

// Unidade vendavel do catalogo (RF03). Peso e dimensoes sao os da embalagem, base do
// frete (D16). estoqueFisico so muda na criacao (saldo inicial) ou por movimento (#13).
// Kit (D7): estoqueFisico nulo; peso e dimensoes nulos sao derivados dos componentes.
@Entity
@Table(name = "variante_produto")
public class VarianteProduto extends DefaultEntity {

    // muitas variantes -> um produto
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(length = 60)
    private String nome;

    @Column(length = 40, unique = true)
    private String sku;

    @Column(precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(precision = 12, scale = 2, name = "custo_aquisicao")
    private BigDecimal custoAquisicao;

    @Column(name = "estoque_fisico")
    private Integer estoqueFisico;

    @Column(nullable = false, name = "estoque_minimo")
    private int estoqueMinimo;

    @Column(precision = 8, scale = 3, name = "peso_embalagem_kg")
    private BigDecimal pesoEmbalagemKg;

    @Column(name = "altura_embalagem_cm")
    private Integer alturaEmbalagemCm;

    @Column(name = "largura_embalagem_cm")
    private Integer larguraEmbalagemCm;

    @Column(name = "comprimento_embalagem_cm")
    private Integer comprimentoEmbalagemCm;

    @Column(nullable = false, name = "eh_kit")
    private boolean ehKit;

    @Column(nullable = false)
    private boolean ativa;

    // so no kit: as variantes que ele reune, cada uma com a quantidade exigida
    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ComponenteKit> componentes = new ArrayList<>();

    // historico do estoque fisico (#13); sai junto com a variante
    @OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimentoEstoque> movimentos = new ArrayList<>();

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public BigDecimal getCustoAquisicao() {
        return custoAquisicao;
    }

    public void setCustoAquisicao(BigDecimal custoAquisicao) {
        this.custoAquisicao = custoAquisicao;
    }

    public Integer getEstoqueFisico() {
        return estoqueFisico;
    }

    public void setEstoqueFisico(Integer estoqueFisico) {
        this.estoqueFisico = estoqueFisico;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public BigDecimal getPesoEmbalagemKg() {
        return pesoEmbalagemKg;
    }

    public void setPesoEmbalagemKg(BigDecimal pesoEmbalagemKg) {
        this.pesoEmbalagemKg = pesoEmbalagemKg;
    }

    public Integer getAlturaEmbalagemCm() {
        return alturaEmbalagemCm;
    }

    public void setAlturaEmbalagemCm(Integer alturaEmbalagemCm) {
        this.alturaEmbalagemCm = alturaEmbalagemCm;
    }

    public Integer getLarguraEmbalagemCm() {
        return larguraEmbalagemCm;
    }

    public void setLarguraEmbalagemCm(Integer larguraEmbalagemCm) {
        this.larguraEmbalagemCm = larguraEmbalagemCm;
    }

    public Integer getComprimentoEmbalagemCm() {
        return comprimentoEmbalagemCm;
    }

    public void setComprimentoEmbalagemCm(Integer comprimentoEmbalagemCm) {
        this.comprimentoEmbalagemCm = comprimentoEmbalagemCm;
    }

    public boolean isEhKit() {
        return ehKit;
    }

    public void setEhKit(boolean ehKit) {
        this.ehKit = ehKit;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public List<ComponenteKit> getComponentes() {
        return componentes;
    }

    public void setComponentes(List<ComponenteKit> componentes) {
        this.componentes = componentes;
    }

    public List<MovimentoEstoque> getMovimentos() {
        return movimentos;
    }

    public void setMovimentos(List<MovimentoEstoque> movimentos) {
        this.movimentos = movimentos;
    }

}

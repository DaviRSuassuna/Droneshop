package br.com.droneshop.catalogo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Heranca JOINED (D27): produto guarda o comum, drone, acessorio e kit guardam o proprio
// e compartilham o id. A coluna tipo guarda o id de TipoProduto. Cada versao e um
// produto (D40): SKU, preco, custo, estoque e embalagem ficam aqui.
@Entity
@Table(name = "produto", uniqueConstraints = @UniqueConstraint(columnNames = { "id_grupo", "rotulo_no_grupo" }))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Produto extends DefaultEntity {

    @Column(length = 120)
    private String nome;

    // unico na loja toda
    @Column(length = 40, unique = true)
    private String sku;

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

    @Column(precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(precision = 12, scale = 2, name = "custo_aquisicao")
    private BigDecimal custoAquisicao;

    // So muda na criacao (saldo inicial) ou por movimento (#13). Nulo no kit (D7).
    @Column(name = "estoque_fisico")
    private Integer estoqueFisico;

    @Column(nullable = false, name = "estoque_minimo")
    private int estoqueMinimo;

    // Embalagem, base do frete (D16). No kit, nulo e derivado dos componentes.
    @Column(precision = 8, scale = 3, name = "peso_embalagem_kg")
    private BigDecimal pesoEmbalagemKg;

    @Column(name = "altura_embalagem_cm")
    private Integer alturaEmbalagemCm;

    @Column(name = "largura_embalagem_cm")
    private Integer larguraEmbalagemCm;

    @Column(name = "comprimento_embalagem_cm")
    private Integer comprimentoEmbalagemCm;

    // muitos produtos -> um grupo (opcional): versoes numa pagina de venda so (D40)
    @ManyToOne
    @JoinColumn(name = "id_grupo")
    private GrupoProduto grupo;

    // obrigatorio com grupo e unico dentro dele ("Pro", "Standard", "Fly More")
    @Column(length = 40, name = "rotulo_no_grupo")
    private String rotuloNoGrupo;

    @Column(nullable = false, name = "ordem_no_grupo")
    private int ordemNoGrupo;

    // historico do estoque fisico (#13); sai junto com o produto
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimentoEstoque> movimentos = new ArrayList<>();

    // lado reciproco da compatibilidade (D17): so leitura, quem grava e o acessorio
    @ManyToMany(mappedBy = "compativeis")
    private Set<Acessorio> acessoriosCompativeis = new HashSet<>();

    public abstract TipoProduto getTipo();

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

    public GrupoProduto getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoProduto grupo) {
        this.grupo = grupo;
    }

    public String getRotuloNoGrupo() {
        return rotuloNoGrupo;
    }

    public void setRotuloNoGrupo(String rotuloNoGrupo) {
        this.rotuloNoGrupo = rotuloNoGrupo;
    }

    public int getOrdemNoGrupo() {
        return ordemNoGrupo;
    }

    public void setOrdemNoGrupo(int ordemNoGrupo) {
        this.ordemNoGrupo = ordemNoGrupo;
    }

    public List<MovimentoEstoque> getMovimentos() {
        return movimentos;
    }

    public void setMovimentos(List<MovimentoEstoque> movimentos) {
        this.movimentos = movimentos;
    }

    public Set<Acessorio> getAcessoriosCompativeis() {
        return acessoriosCompativeis;
    }

}

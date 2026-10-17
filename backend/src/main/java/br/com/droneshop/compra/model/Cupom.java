package br.com.droneshop.compra.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "cupom")
public class Cupom extends DefaultEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, name = "tipo_desconto")
    private TipoDescontoCupom tipoDesconto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(precision = 12, scale = 2, name = "valor_minimo")
    private BigDecimal valorMinimo;

    @Column(name = "limite_total_uso")
    private Integer limiteTotalUso;

    @Column(name = "limite_por_cliente")
    private Integer limitePorCliente;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToMany
    @JoinTable(name = "cupom_categoria",
            joinColumns = @JoinColumn(name = "id_cupom"),
            inverseJoinColumns = @JoinColumn(name = "id_categoria"))
    private List<Categoria> categorias = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "cupom_marca",
            joinColumns = @JoinColumn(name = "id_cupom"),
            inverseJoinColumns = @JoinColumn(name = "id_marca"))
    private List<Marca> marcas = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "cupom_variante",
            joinColumns = @JoinColumn(name = "id_cupom"),
            inverseJoinColumns = @JoinColumn(name = "id_variante"))
    private List<VarianteProduto> variantes = new ArrayList<>();

    @OneToMany(mappedBy = "cupom")
    private List<UsoCupom> usos = new ArrayList<>();

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public TipoDescontoCupom getTipoDesconto() { return tipoDesconto; }
    public void setTipoDesconto(TipoDescontoCupom tipoDesconto) { this.tipoDesconto = tipoDesconto; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }
    public BigDecimal getValorMinimo() { return valorMinimo; }
    public void setValorMinimo(BigDecimal valorMinimo) { this.valorMinimo = valorMinimo; }
    public Integer getLimiteTotalUso() { return limiteTotalUso; }
    public void setLimiteTotalUso(Integer limiteTotalUso) { this.limiteTotalUso = limiteTotalUso; }
    public Integer getLimitePorCliente() { return limitePorCliente; }
    public void setLimitePorCliente(Integer limitePorCliente) { this.limitePorCliente = limitePorCliente; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public List<Categoria> getCategorias() { return categorias; }
    public void setCategorias(List<Categoria> categorias) { this.categorias = categorias; }
    public List<Marca> getMarcas() { return marcas; }
    public void setMarcas(List<Marca> marcas) { this.marcas = marcas; }
    public List<VarianteProduto> getVariantes() { return variantes; }
    public void setVariantes(List<VarianteProduto> variantes) { this.variantes = variantes; }
    public List<UsoCupom> getUsos() { return usos; }
}

package br.com.droneshop.catalogo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.pedido.model.DescontoPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "promocao")
public class Promocao extends DefaultEntity {

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private EscopoPromocao escopo;

    @Column(nullable = false, name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(nullable = false, name = "data_fim")
    private LocalDateTime dataFim;

    @Column(precision = 5, scale = 2, name = "percentual_desconto")
    private BigDecimal percentualDesconto;

    @Column(precision = 12, scale = 2, name = "valor_desconto")
    private BigDecimal valorDesconto;

    @Column(precision = 12, scale = 2, name = "valor_minimo_carrinho")
    private BigDecimal valorMinimoCarrinho;

    @Column(nullable = false)
    private boolean ativa;

    @ManyToMany
    @JoinTable(name = "promocao_variante",
            joinColumns = @JoinColumn(name = "id_promocao"),
            inverseJoinColumns = @JoinColumn(name = "id_variante"))
    private List<VarianteProduto> variantes = new ArrayList<>();

    @OneToMany(mappedBy = "promocao")
    private List<DescontoPedido> descontos = new ArrayList<>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public EscopoPromocao getEscopo() {
        return escopo;
    }

    public void setEscopo(EscopoPromocao escopo) {
        this.escopo = escopo;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setPercentualDesconto(BigDecimal percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public BigDecimal getValorMinimoCarrinho() {
        return valorMinimoCarrinho;
    }

    public void setValorMinimoCarrinho(BigDecimal valorMinimoCarrinho) {
        this.valorMinimoCarrinho = valorMinimoCarrinho;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public List<VarianteProduto> getVariantes() {
        return variantes;
    }

    public void setVariantes(List<VarianteProduto> variantes) {
        this.variantes = variantes;
    }

    public List<DescontoPedido> getDescontos() {
        return descontos;
    }
}

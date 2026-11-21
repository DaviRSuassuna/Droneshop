package br.com.droneshop.pedido.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Entrega do pedido com custo e prazo congelados (RN07). */
@Entity
@Table(name = "entrega")
public class Entrega extends DefaultEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Column(nullable = false)
    private ModalidadeEntrega modalidade;

    @Column(name = "origem_calculo", nullable = false)
    private OrigemCalculoFrete origemCalculo;

    @Column(length = 80)
    private String transportadora;

    @Column(name = "codigo_servico_transportadora", length = 60)
    private String codigoServicoTransportadora;

    @Column(name = "custo_congelado", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoCongelado;

    @Column(name = "prazo_congelado_dias", nullable = false)
    private int prazoCongeladoDias;

    @Column(name = "codigo_rastreio", length = 60)
    private String codigoRastreio;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora, id")
    private List<EventoRastreio> eventos = new ArrayList<>();

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public ModalidadeEntrega getModalidade() {
        return modalidade;
    }

    public void setModalidade(ModalidadeEntrega modalidade) {
        this.modalidade = modalidade;
    }

    public OrigemCalculoFrete getOrigemCalculo() {
        return origemCalculo;
    }

    public void setOrigemCalculo(OrigemCalculoFrete origemCalculo) {
        this.origemCalculo = origemCalculo;
    }

    public String getTransportadora() {
        return transportadora;
    }

    public void setTransportadora(String transportadora) {
        this.transportadora = transportadora;
    }

    public String getCodigoServicoTransportadora() {
        return codigoServicoTransportadora;
    }

    public void setCodigoServicoTransportadora(String codigoServicoTransportadora) {
        this.codigoServicoTransportadora = codigoServicoTransportadora;
    }

    public BigDecimal getCustoCongelado() {
        return custoCongelado;
    }

    public void setCustoCongelado(BigDecimal custoCongelado) {
        this.custoCongelado = custoCongelado;
    }

    public int getPrazoCongeladoDias() {
        return prazoCongeladoDias;
    }

    public void setPrazoCongeladoDias(int prazoCongeladoDias) {
        this.prazoCongeladoDias = prazoCongeladoDias;
    }

    public String getCodigoRastreio() {
        return codigoRastreio;
    }

    public void setCodigoRastreio(String codigoRastreio) {
        this.codigoRastreio = codigoRastreio;
    }

    public List<EventoRastreio> getEventos() {
        return eventos;
    }

    public void setEventos(List<EventoRastreio> eventos) {
        this.eventos = eventos;
    }
}

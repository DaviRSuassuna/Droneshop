package br.com.droneshop.estoque.model;

import java.time.LocalDate;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.pedido.model.ItemPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Drone rastreado por número de série; vinculado ao item na expedição (RN12, RN14). */
@Entity
@Table(name = "unidade_individual")
public class UnidadeIndividual extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private StatusUnidade status;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 60)
    private String numeroSerie;

    @Column(name = "registro_anac", length = 40)
    private String registroAnac;

    @Column(name = "validade_garantia")
    private LocalDate validadeGarantia;

    @ManyToOne
    @JoinColumn(name = "id_item_pedido")
    private ItemPedido itemPedido;

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public StatusUnidade getStatus() {
        return status;
    }

    public void setStatus(StatusUnidade status) {
        this.status = status;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getRegistroAnac() {
        return registroAnac;
    }

    public void setRegistroAnac(String registroAnac) {
        this.registroAnac = registroAnac;
    }

    public LocalDate getValidadeGarantia() {
        return validadeGarantia;
    }

    public void setValidadeGarantia(LocalDate validadeGarantia) {
        this.validadeGarantia = validadeGarantia;
    }

    public ItemPedido getItemPedido() {
        return itemPedido;
    }

    public void setItemPedido(ItemPedido itemPedido) {
        this.itemPedido = itemPedido;
    }
}

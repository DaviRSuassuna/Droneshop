package br.com.droneshop.pedido.model;

import java.math.BigDecimal;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_pedido")
public class ItemPedido extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    // RN06: preço do produto no momento da compra; mudar o catálogo depois não altera o pedido
    @Column(name = "preco_unitario_congelado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitarioCongelado;

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitarioCongelado() {
        return precoUnitarioCongelado;
    }

    public void setPrecoUnitarioCongelado(BigDecimal precoUnitarioCongelado) {
        this.precoUnitarioCongelado = precoUnitarioCongelado;
    }
}

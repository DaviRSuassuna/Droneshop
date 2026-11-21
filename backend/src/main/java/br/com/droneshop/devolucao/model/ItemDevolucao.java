package br.com.droneshop.devolucao.model;

import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.pedido.model.ItemPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Item devolvido e quantidade. */
@Entity
@Table(name = "item_devolucao")
public class ItemDevolucao extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_devolucao", nullable = false)
    private Devolucao devolucao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_item_pedido", nullable = false)
    private ItemPedido itemPedido;

    @Column(nullable = false)
    private int quantidade;

    public Devolucao getDevolucao() {
        return devolucao;
    }

    public void setDevolucao(Devolucao devolucao) {
        this.devolucao = devolucao;
    }

    public ItemPedido getItemPedido() {
        return itemPedido;
    }

    public void setItemPedido(ItemPedido itemPedido) {
        this.itemPedido = itemPedido;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}

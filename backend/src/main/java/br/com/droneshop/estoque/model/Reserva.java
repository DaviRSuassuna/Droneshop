package br.com.droneshop.estoque.model;

import java.time.LocalDateTime;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.compra.model.ItemCarrinho;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Reserva de estoque do checkout (#23); kit reserva cada componente (D7). */
@Entity
@Table(name = "reserva")
public class Reserva extends DefaultEntity {

    @Column(nullable = false)
    private StatusReserva status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "id_item_carrinho")
    private ItemCarrinho itemCarrinho;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public ItemCarrinho getItemCarrinho() {
        return itemCarrinho;
    }

    public void setItemCarrinho(ItemCarrinho itemCarrinho) {
        this.itemCarrinho = itemCarrinho;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }
}

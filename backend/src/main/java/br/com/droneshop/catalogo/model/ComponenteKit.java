package br.com.droneshop.catalogo.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Item do kit (D7): quantas unidades de outro produto o kit leva. A baixa de estoque
// acontece sempre no componente, nunca no kit.
@Entity
@Table(name = "componente_kit", uniqueConstraints = @UniqueConstraint(columnNames = { "id_kit", "id_produto" }))
public class ComponenteKit extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_kit", nullable = false)
    private Kit kit;

    // nunca e outro kit
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
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

}

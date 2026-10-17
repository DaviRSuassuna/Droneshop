package br.com.droneshop.catalogo.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Item do kit (D7): quantas unidades de outra variante o kit leva. A baixa de estoque
// acontece sempre no componente, nunca no kit.
@Entity
@Table(name = "componente_kit", uniqueConstraints = @UniqueConstraint(columnNames = { "id_kit", "id_componente" }))
public class ComponenteKit extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_kit", nullable = false)
    private VarianteProduto kit;

    // nunca e outro kit
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_componente", nullable = false)
    private VarianteProduto componente;

    @Column(nullable = false)
    private int quantidade;

    public VarianteProduto getKit() {
        return kit;
    }

    public void setKit(VarianteProduto kit) {
        this.kit = kit;
    }

    public VarianteProduto getComponente() {
        return componente;
    }

    public void setComponente(VarianteProduto componente) {
        this.componente = componente;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

}

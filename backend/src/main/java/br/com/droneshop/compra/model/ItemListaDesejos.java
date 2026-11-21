package br.com.droneshop.compra.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "item_lista_desejos", uniqueConstraints = @UniqueConstraint(columnNames = { "id_lista", "id_produto" }))
public class ItemListaDesejos extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_lista", nullable = false)
    private ListaDesejos lista;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(name = "adicionado_em", nullable = false)
    private LocalDateTime adicionadoEm;

    /** Preço quando foi favoritado: base do aviso de queda de preço (#86). */
    @Column(name = "preco_ao_adicionar", precision = 12, scale = 2)
    private BigDecimal precoAoAdicionar;

    public ListaDesejos getLista() {
        return lista;
    }

    public void setLista(ListaDesejos lista) {
        this.lista = lista;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public LocalDateTime getAdicionadoEm() {
        return adicionadoEm;
    }

    public void setAdicionadoEm(LocalDateTime adicionadoEm) {
        this.adicionadoEm = adicionadoEm;
    }

    public BigDecimal getPrecoAoAdicionar() {
        return precoAoAdicionar;
    }

    public void setPrecoAoAdicionar(BigDecimal precoAoAdicionar) {
        this.precoAoAdicionar = precoAoAdicionar;
    }
}

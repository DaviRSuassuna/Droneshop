package br.com.droneshop.catalogo.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
@DiscriminatorValue("2")
public class Acessorio extends Produto {

    @Column(length = 60, name = "tipo_acessorio")
    private String tipoAcessorio;

    // Compatibilidade N:N (D17), gravada pelo cadastro do acessorio. O destino e Produto,
    // entao cobre acessorio-drone e acessorio-acessorio; o outro lado le por
    // Produto.acessoriosCompativeis.
    @ManyToMany
    @JoinTable(name = "acessorio_compativel",
            joinColumns = @JoinColumn(name = "id_acessorio"),
            inverseJoinColumns = @JoinColumn(name = "id_produto"))
    private Set<Produto> compativeis = new HashSet<>();

    @Override
    public TipoProduto getTipo() {
        return TipoProduto.ACESSORIO;
    }

    public String getTipoAcessorio() {
        return tipoAcessorio;
    }

    public void setTipoAcessorio(String tipoAcessorio) {
        this.tipoAcessorio = tipoAcessorio;
    }

    public Set<Produto> getCompativeis() {
        return compativeis;
    }

}

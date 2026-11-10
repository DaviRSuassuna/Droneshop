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

    // Peso do acessorio em si, em gramas (#84); o da embalagem e pesoEmbalagemKg (RN18)
    @Column(name = "peso_item_g")
    private Integer pesoItemG;

    // Na pagina do drone so entram os compativeis com esta opcao ligada (#84)
    @Column(nullable = false, name = "sugerir_nos_compativeis", columnDefinition = "boolean default false")
    private boolean sugerirNosCompativeis;

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

    public Integer getPesoItemG() {
        return pesoItemG;
    }

    public void setPesoItemG(Integer pesoItemG) {
        this.pesoItemG = pesoItemG;
    }

    public boolean isSugerirNosCompativeis() {
        return sugerirNosCompativeis;
    }

    public void setSugerirNosCompativeis(boolean sugerirNosCompativeis) {
        this.sugerirNosCompativeis = sugerirNosCompativeis;
    }

    public Set<Produto> getCompativeis() {
        return compativeis;
    }

}

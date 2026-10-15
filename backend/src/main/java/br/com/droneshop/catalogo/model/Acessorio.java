package br.com.droneshop.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("2")
public class Acessorio extends Produto {

    @Column(length = 60, name = "tipo_acessorio")
    private String tipoAcessorio;

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

}

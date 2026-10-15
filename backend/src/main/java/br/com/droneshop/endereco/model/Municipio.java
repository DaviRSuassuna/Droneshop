package br.com.droneshop.endereco.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Dados oficiais do IBGE, carregados por localidades.sql (somente leitura na API).
@Entity
public class Municipio extends DefaultEntity {

    @Column(length = 60, nullable = false)
    private String nome;

    // codigo IBGE de 7 digitos (ex.: 1721000 = Palmas/TO); e o que o ViaCEP devolve no campo "ibge"
    @Column(nullable = false, unique = true, name = "codigo_ibge")
    private Integer codigoIbge;

    // muitos municipios -> um estado
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getCodigoIbge() {
        return codigoIbge;
    }

    public void setCodigoIbge(Integer codigoIbge) {
        this.codigoIbge = codigoIbge;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

}

package br.com.droneshop.endereco.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

// Dados oficiais do IBGE, carregados por localidades.sql (somente leitura na API).
@Entity
public class Estado extends DefaultEntity {

    @Column(length = 60, nullable = false)
    private String nome;

    @Column(length = 2, nullable = false, unique = true)
    private String sigla;

    // codigo IBGE da UF (ex.: 17 = Tocantins)
    @Column(nullable = false, unique = true, name = "codigo_ibge")
    private Integer codigoIbge;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public Integer getCodigoIbge() {
        return codigoIbge;
    }

    public void setCodigoIbge(Integer codigoIbge) {
        this.codigoIbge = codigoIbge;
    }

}

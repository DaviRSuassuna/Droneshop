package br.com.droneshop.endereco.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Nao e tabela: as colunas ficam dentro da tabela de quem embute (hoje, fornecedor).
// Cidade e UF nao sao texto: o endereco aponta para Municipio, e o Estado vem por ele.
@Embeddable
public class Endereco {

    // so os 8 digitos, sem hifen; a mascara 00000-000 e so do front
    @Column(length = 8, nullable = false)
    private String cep;

    @Column(length = 150, nullable = false)
    private String logradouro;

    @Column(length = 10, nullable = false)
    private String numero;

    @Column(length = 60)
    private String complemento;

    @Column(length = 80, nullable = false)
    private String bairro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "municipio_id", nullable = false)
    private Municipio municipio;

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

}

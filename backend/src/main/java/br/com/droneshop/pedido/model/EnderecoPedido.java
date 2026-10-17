package br.com.droneshop.pedido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Fotografia do endereço na compra (D12, RN15): cidade e UF como texto, CEP só com dígitos (D32)
@Embeddable
public class EnderecoPedido {

    @Column(name = "entrega_destinatario", length = 150)
    private String destinatario;

    @Column(name = "entrega_logradouro", length = 150)
    private String logradouro;

    @Column(name = "entrega_numero", length = 10)
    private String numero;

    @Column(name = "entrega_complemento", length = 60)
    private String complemento;

    @Column(name = "entrega_bairro", length = 80)
    private String bairro;

    @Column(name = "entrega_cidade", length = 100)
    private String cidade;

    @Column(name = "entrega_uf", length = 2)
    private String uf;

    @Column(name = "entrega_cep", length = 8)
    private String cep;

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
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

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }
}

package br.com.droneshop.catalogo.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Fornecedor extends DefaultEntity {

    @Column(length = 150, nullable = false, name = "razao_social")
    private String razaoSocial;

    // armazenado sempre no formato 00.000.000/0000-00 (normalizado no service)
    @Column(length = 18, nullable = false, unique = true)
    private String cnpj;

    // contato em tres colunas proprias (D18): permite busca e validacao de formato
    @Column(length = 100, nullable = false, name = "nome_contato")
    private String nomeContato;

    @Column(length = 150, nullable = false, name = "email_contato")
    private String emailContato;

    @Column(length = 20, name = "telefone_contato")
    private String telefoneContato;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StatusFornecedor status;

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNomeContato() {
        return nomeContato;
    }

    public void setNomeContato(String nomeContato) {
        this.nomeContato = nomeContato;
    }

    public String getEmailContato() {
        return emailContato;
    }

    public void setEmailContato(String emailContato) {
        this.emailContato = emailContato;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public void setTelefoneContato(String telefoneContato) {
        this.telefoneContato = telefoneContato;
    }

    public StatusFornecedor getStatus() {
        return status;
    }

    public void setStatus(StatusFornecedor status) {
        this.status = status;
    }

}

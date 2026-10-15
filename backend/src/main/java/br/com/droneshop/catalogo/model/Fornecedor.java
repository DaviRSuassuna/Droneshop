package br.com.droneshop.catalogo.model;

import br.com.droneshop.endereco.model.Endereco;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

@Entity
public class Fornecedor extends DefaultEntity {

    @Column(length = 150, nullable = false, name = "razao_social")
    private String razaoSocial;

    // armazenado so com os 14 digitos, sem mascara (normalizado no service);
    // a mascara 00.000.000/0000-00 aparece apenas no front
    @Column(length = 14, nullable = false, unique = true)
    private String cnpj;

    // contato em tres colunas proprias (D18): permite busca e validacao de formato
    @Column(length = 100, nullable = false, name = "nome_contato")
    private String nomeContato;

    @Column(length = 150, nullable = false, name = "email_contato")
    private String emailContato;

    // so digitos: DDD + 8 ou 9 digitos (ex.: 63999998888); a mascara e so do front
    @Column(length = 11, name = "telefone_contato")
    private String telefoneContato;

    // endereco obrigatorio; Municipio e Estado estruturados (tabelas), nao texto
    @Embedded
    private Endereco endereco;

    @Column(nullable = false)
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

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public StatusFornecedor getStatus() {
        return status;
    }

    public void setStatus(StatusFornecedor status) {
        this.status = status;
    }

}

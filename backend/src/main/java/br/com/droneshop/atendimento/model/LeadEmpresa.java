package br.com.droneshop.atendimento.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Pedido de proposta comercial feito pela página Empresas, sem login (D36). */
@Entity
@Table(name = "lead_empresa")
public class LeadEmpresa extends DefaultEntity {

    @Column(nullable = false, length = 150)
    private String razaoSocial;

    @Column(nullable = false, length = 14)
    private String cnpj;

    @Column(nullable = false, length = 120)
    private String nomeContato;

    @Column(nullable = false, length = 150)
    private String emailContato;

    @Column(length = 20)
    private String telefoneContato;

    @Column(length = 80)
    private String segmento;

    private Integer quantidadeEstimada;

    @Column(length = 2000)
    private String mensagem;

    @Column(nullable = false)
    private StatusLead status;

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getNomeContato() { return nomeContato; }
    public void setNomeContato(String nomeContato) { this.nomeContato = nomeContato; }
    public String getEmailContato() { return emailContato; }
    public void setEmailContato(String emailContato) { this.emailContato = emailContato; }
    public String getTelefoneContato() { return telefoneContato; }
    public void setTelefoneContato(String telefoneContato) { this.telefoneContato = telefoneContato; }
    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }
    public Integer getQuantidadeEstimada() { return quantidadeEstimada; }
    public void setQuantidadeEstimada(Integer quantidadeEstimada) { this.quantidadeEstimada = quantidadeEstimada; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public StatusLead getStatus() { return status; }
    public void setStatus(StatusLead status) { this.status = status; }
}

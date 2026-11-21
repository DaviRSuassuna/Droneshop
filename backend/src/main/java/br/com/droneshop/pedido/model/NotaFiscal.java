package br.com.droneshop.pedido.model;

import java.time.LocalDateTime;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** NF-e simulada: identificadores fictícios gerados localmente, sem SEFAZ (#35). */
@Entity
@Table(name = "nota_fiscal")
public class NotaFiscal extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(name = "tipo_origem", nullable = false)
    private TipoOrigemNota tipoOrigem;

    @Column(nullable = false, length = 9)
    private String numero;

    @Column(nullable = false, length = 3)
    private String serie;

    @Column(name = "chave_acesso", nullable = false, unique = true, length = 44)
    private String chaveAcesso;

    @Column(name = "xml_url", length = 200)
    private String xmlUrl;

    @Column(name = "danfe_url", length = 200)
    private String danfeUrl;

    @Column(name = "data_emissao", nullable = false)
    private LocalDateTime dataEmissao;

    @Column(name = "id_devolucao")
    private Long idDevolucao;

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public TipoOrigemNota getTipoOrigem() {
        return tipoOrigem;
    }

    public void setTipoOrigem(TipoOrigemNota tipoOrigem) {
        this.tipoOrigem = tipoOrigem;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public void setChaveAcesso(String chaveAcesso) {
        this.chaveAcesso = chaveAcesso;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }

    public String getDanfeUrl() {
        return danfeUrl;
    }

    public void setDanfeUrl(String danfeUrl) {
        this.danfeUrl = danfeUrl;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public Long getIdDevolucao() {
        return idDevolucao;
    }

    public void setIdDevolucao(Long idDevolucao) {
        this.idDevolucao = idDevolucao;
    }
}

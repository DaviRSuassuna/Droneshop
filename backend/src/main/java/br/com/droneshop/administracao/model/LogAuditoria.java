package br.com.droneshop.administracao.model;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Uma linha por campo alterado (#41, RF45). Gravada pelo AuditoriaInterceptor direto na
 * conexão da transação; dado pessoal aparece como *** (RNF03). dataCadastro é a data/hora.
 */
@Entity
@Table(name = "log_auditoria")
public class LogAuditoria extends DefaultEntity {

    @Column(nullable = false)
    private AcaoAuditoria acao;

    @Column(nullable = false, length = 80)
    private String entidade;

    @Column(name = "entidade_id", length = 60)
    private String entidadeId;

    @Column(length = 80)
    private String campo;

    @Column(name = "valor_anterior", columnDefinition = "text")
    private String valorAnterior;

    @Column(name = "valor_novo", columnDefinition = "text")
    private String valorNovo;

    @ManyToOne
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    @Column(length = 60)
    private String ip;

    public AcaoAuditoria getAcao() { return acao; }
    public String getEntidade() { return entidade; }
    public String getEntidadeId() { return entidadeId; }
    public String getCampo() { return campo; }
    public String getValorAnterior() { return valorAnterior; }
    public String getValorNovo() { return valorNovo; }
    public Usuario getAutor() { return autor; }
    public String getIp() { return ip; }
}

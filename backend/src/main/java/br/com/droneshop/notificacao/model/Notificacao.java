package br.com.droneshop.notificacao.model;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** E-mail enviado; textoRenderizado é o histórico do que saiu (RF38, RF40). */
@Entity
@Table(name = "notificacao")
public class Notificacao extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_template", nullable = false)
    private TemplateNotificacao template;

    @Column(nullable = false)
    private CategoriaNotificacao categoria;

    @Column(nullable = false)
    private StatusEnvioNotificacao statusEnvio;

    /** Usuário destinatário; nulo quando o e-mail vai para um contato sem conta (lead). */
    @ManyToOne
    @JoinColumn(name = "id_destinatario")
    private Usuario destinatario;

    @Column(nullable = false, length = 200)
    private String emailDestino;

    @Column(nullable = false, length = 200)
    private String assunto;

    @Column(nullable = false, columnDefinition = "text")
    private String textoRenderizado;

    @Column(length = 120)
    private String identificadorProvedor;

    public TemplateNotificacao getTemplate() { return template; }
    public void setTemplate(TemplateNotificacao template) { this.template = template; }
    public CategoriaNotificacao getCategoria() { return categoria; }
    public void setCategoria(CategoriaNotificacao categoria) { this.categoria = categoria; }
    public StatusEnvioNotificacao getStatusEnvio() { return statusEnvio; }
    public void setStatusEnvio(StatusEnvioNotificacao statusEnvio) { this.statusEnvio = statusEnvio; }
    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }
    public String getEmailDestino() { return emailDestino; }
    public void setEmailDestino(String emailDestino) { this.emailDestino = emailDestino; }
    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public String getTextoRenderizado() { return textoRenderizado; }
    public void setTextoRenderizado(String textoRenderizado) { this.textoRenderizado = textoRenderizado; }
    public String getIdentificadorProvedor() { return identificadorProvedor; }
    public void setIdentificadorProvedor(String identificadorProvedor) { this.identificadorProvedor = identificadorProvedor; }
}

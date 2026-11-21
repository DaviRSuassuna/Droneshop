package br.com.droneshop.atendimento.model;

import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.pedido.model.Pedido;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Chamado de suporte com protocolo legível AER-AAAAMMDD-NNNN (#38, RF33, RF34). */
@Entity
@Table(name = "chamado")
public class Chamado extends DefaultEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String protocolo;

    @Column(nullable = false)
    private StatusChamado status;

    @Column(nullable = false, length = 150)
    private String assunto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_perfil_cliente", nullable = false)
    private PerfilCliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_atendente")
    private Usuario atendente;

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<MensagemChamado> mensagens = new ArrayList<>();

    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }
    public StatusChamado getStatus() { return status; }
    public void setStatus(StatusChamado status) { this.status = status; }
    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public PerfilCliente getCliente() { return cliente; }
    public void setCliente(PerfilCliente cliente) { this.cliente = cliente; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Usuario getAtendente() { return atendente; }
    public void setAtendente(Usuario atendente) { this.atendente = atendente; }
    public List<MensagemChamado> getMensagens() { return mensagens; }
}

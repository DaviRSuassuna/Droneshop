package br.com.droneshop.pedido.model;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.model.OrigemRegistro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// A data da transição é o dataCadastro. Sem rotas nesta issue (a #17 grava e expõe).
@Entity
@Table(name = "historico_status_pedido")
public class HistoricoStatusPedido extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    // nulo na criação do pedido
    @Column(name = "status_anterior")
    private StatusPedido statusAnterior;

    @Column(name = "status_novo", nullable = false)
    private StatusPedido statusNovo;

    @Column(nullable = false)
    private OrigemRegistro origem;

    // D11: só quando a origem é MANUAL
    @ManyToOne
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    @Column(length = 500)
    private String observacao;

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public StatusPedido getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(StatusPedido statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public StatusPedido getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(StatusPedido statusNovo) {
        this.statusNovo = statusNovo;
    }

    public OrigemRegistro getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemRegistro origem) {
        this.origem = origem;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}

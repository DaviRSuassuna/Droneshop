package br.com.droneshop.devolucao.model;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.model.OrigemRegistro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Histórico da devolução com origem e autor (D11). */
@Entity
@Table(name = "historico_devolucao")
public class HistoricoDevolucao extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_devolucao", nullable = false)
    private Devolucao devolucao;

    @Column(name = "status_anterior")
    private StatusDevolucao statusAnterior;

    @Column(name = "status_novo", nullable = false)
    private StatusDevolucao statusNovo;

    @Column(nullable = false)
    private OrigemRegistro origem;

    @ManyToOne
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    @Column(length = 500)
    private String observacao;

    public Devolucao getDevolucao() {
        return devolucao;
    }

    public void setDevolucao(Devolucao devolucao) {
        this.devolucao = devolucao;
    }

    public StatusDevolucao getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(StatusDevolucao statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public StatusDevolucao getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(StatusDevolucao statusNovo) {
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

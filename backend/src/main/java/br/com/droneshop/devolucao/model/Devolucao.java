package br.com.droneshop.devolucao.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

/** Devolução ou troca de itens de um pedido entregue (RF30, RN09). */
@Entity
@Table(name = "devolucao")
public class Devolucao extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(nullable = false)
    private StatusDevolucao status;

    @Column(nullable = false)
    private ResolucaoDevolucao resolucao;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @OneToMany(mappedBy = "devolucao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemDevolucao> itens = new ArrayList<>();

    @OneToMany(mappedBy = "devolucao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataCadastro, id")
    private List<HistoricoDevolucao> historico = new ArrayList<>();

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public StatusDevolucao getStatus() {
        return status;
    }

    public void setStatus(StatusDevolucao status) {
        this.status = status;
    }

    public ResolucaoDevolucao getResolucao() {
        return resolucao;
    }

    public void setResolucao(ResolucaoDevolucao resolucao) {
        this.resolucao = resolucao;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public List<ItemDevolucao> getItens() {
        return itens;
    }

    public void setItens(List<ItemDevolucao> itens) {
        this.itens = itens;
    }

    public List<HistoricoDevolucao> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoDevolucao> historico) {
        this.historico = historico;
    }
}

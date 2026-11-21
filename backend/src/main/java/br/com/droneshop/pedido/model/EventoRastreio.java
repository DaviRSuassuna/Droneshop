package br.com.droneshop.pedido.model;

import java.time.LocalDateTime;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Evento de rastreio da entrega (#31). */
@Entity
@Table(name = "evento_rastreio")
public class EventoRastreio extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_entrega", nullable = false)
    private Entrega entrega;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(length = 120)
    private String localizacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}

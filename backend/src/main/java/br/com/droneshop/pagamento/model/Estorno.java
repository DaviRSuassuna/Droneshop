package br.com.droneshop.pagamento.model;

import java.math.BigDecimal;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Estorno total ou parcial do pagamento (#36). */
@Entity
@Table(name = "estorno")
public class Estorno extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pagamento", nullable = false)
    private Pagamento pagamento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Column(name = "gateway_estorno_id", length = 60)
    private String gatewayEstornoId;

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getGatewayEstornoId() {
        return gatewayEstornoId;
    }

    public void setGatewayEstornoId(String gatewayEstornoId) {
        this.gatewayEstornoId = gatewayEstornoId;
    }
}

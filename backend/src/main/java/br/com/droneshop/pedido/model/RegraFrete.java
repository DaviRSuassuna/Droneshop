package br.com.droneshop.pedido.model;

import java.math.BigDecimal;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Custo fixo por modalidade: retirada no showroom e fallback do agregador (#30). */
@Entity
@Table(name = "regra_frete")
public class RegraFrete extends DefaultEntity {

    @Column(nullable = false)
    private ModalidadeEntrega modalidade;

    @Column(name = "custo_fixo", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoFixo;

    @Column(name = "prazo_dias", nullable = false)
    private int prazoDias;

    @Column(name = "cep_origem", length = 8)
    private String cepOrigem;

    public ModalidadeEntrega getModalidade() {
        return modalidade;
    }

    public void setModalidade(ModalidadeEntrega modalidade) {
        this.modalidade = modalidade;
    }

    public BigDecimal getCustoFixo() {
        return custoFixo;
    }

    public void setCustoFixo(BigDecimal custoFixo) {
        this.custoFixo = custoFixo;
    }

    public int getPrazoDias() {
        return prazoDias;
    }

    public void setPrazoDias(int prazoDias) {
        this.prazoDias = prazoDias;
    }

    public String getCepOrigem() {
        return cepOrigem;
    }

    public void setCepOrigem(String cepOrigem) {
        this.cepOrigem = cepOrigem;
    }
}

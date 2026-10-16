package br.com.droneshop.administracao.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracao_loja")
public class ConfiguracaoLoja extends DefaultEntity {

    @Column(name = "dados_fiscais", nullable = false, length = 2000)
    private String dadosFiscais;

    @Column(name = "cep_origem_frete", nullable = false, length = 8)
    private String cepOrigemFrete;

    @Column(name = "remetente_padrao", nullable = false, length = 150)
    private String remetentePadrao;

    @Column(name = "prazo_arrependimento_dias", nullable = false)
    private int prazoArrependimentoDias;

    @Column(name = "prazo_garantia_dias", nullable = false)
    private int prazoGarantiaDias;

    @Column(name = "prazo_analise_devolucao_dias_uteis", nullable = false)
    private int prazoAnaliseDevolucaoDiasUteis;

    @Column(name = "frete_reverso_por_conta_loja", nullable = false)
    private boolean freteReversoPorContaLoja;

    public String getDadosFiscais() {
        return dadosFiscais;
    }

    public void setDadosFiscais(String dadosFiscais) {
        this.dadosFiscais = dadosFiscais;
    }

    public String getCepOrigemFrete() {
        return cepOrigemFrete;
    }

    public void setCepOrigemFrete(String cepOrigemFrete) {
        this.cepOrigemFrete = cepOrigemFrete;
    }

    public String getRemetentePadrao() {
        return remetentePadrao;
    }

    public void setRemetentePadrao(String remetentePadrao) {
        this.remetentePadrao = remetentePadrao;
    }

    public int getPrazoArrependimentoDias() {
        return prazoArrependimentoDias;
    }

    public void setPrazoArrependimentoDias(int prazoArrependimentoDias) {
        this.prazoArrependimentoDias = prazoArrependimentoDias;
    }

    public int getPrazoGarantiaDias() {
        return prazoGarantiaDias;
    }

    public void setPrazoGarantiaDias(int prazoGarantiaDias) {
        this.prazoGarantiaDias = prazoGarantiaDias;
    }

    public int getPrazoAnaliseDevolucaoDiasUteis() {
        return prazoAnaliseDevolucaoDiasUteis;
    }

    public void setPrazoAnaliseDevolucaoDiasUteis(int prazoAnaliseDevolucaoDiasUteis) {
        this.prazoAnaliseDevolucaoDiasUteis = prazoAnaliseDevolucaoDiasUteis;
    }

    public boolean isFreteReversoPorContaLoja() {
        return freteReversoPorContaLoja;
    }

    public void setFreteReversoPorContaLoja(boolean freteReversoPorContaLoja) {
        this.freteReversoPorContaLoja = freteReversoPorContaLoja;
    }
}

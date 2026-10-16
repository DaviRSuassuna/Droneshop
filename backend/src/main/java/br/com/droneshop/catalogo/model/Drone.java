package br.com.droneshop.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

// Specs como colunas fixas e tipadas (D14). Ficam nulas no rascunho; pesoGramas e
// spec de exibicao e filtro, o peso que cota frete e o da embalagem (variante).
@Entity
@DiscriminatorValue("1")
public class Drone extends Produto {

    @Column(name = "peso_gramas")
    private Integer pesoGramas;

    @Column(length = 30, name = "dimensoes_dobrado")
    private String dimensoesDobrado;

    @Column(length = 30, name = "dimensoes_aberto")
    private String dimensoesAberto;

    @Column(name = "autonomia_minutos")
    private Integer autonomiaMinutos;

    @Column(name = "alcance_metros")
    private Integer alcanceMetros;

    @Column(name = "velocidade_maxima_kmh")
    private Integer velocidadeMaximaKmh;

    @Column(name = "resolucao_foto_mp")
    private Integer resolucaoFotoMp;

    @Column(length = 60, name = "tipo_sensor")
    private String tipoSensor;

    @Column(length = 30, name = "resolucao_video")
    private String resolucaoVideo;

    @Column(length = 60, name = "estabilizacao")
    private String estabilizacao;

    @Column(length = 40, name = "tipo_bateria")
    private String tipoBateria;

    @Column(name = "capacidade_bateria_mah")
    private Integer capacidadeBateriaMah;

    @Column(name = "tempo_carregamento_minutos")
    private Integer tempoCarregamentoMinutos;

    @Column(nullable = false, name = "gps_integrado")
    private boolean gpsIntegrado;

    @Column(nullable = false, name = "sensores_anticolisao")
    private boolean sensoresAnticolisao;

    @Column(nullable = false, name = "exige_registro_anac")
    private boolean exigeRegistroAnac;

    @Column(length = 30, name = "classe_anac")
    private String classeAnac;

    @Column(length = 40, name = "registro_sisant")
    private String registroSisant;

    @Override
    public TipoProduto getTipo() {
        return TipoProduto.DRONE;
    }

    public Integer getPesoGramas() {
        return pesoGramas;
    }

    public void setPesoGramas(Integer pesoGramas) {
        this.pesoGramas = pesoGramas;
    }

    public String getDimensoesDobrado() {
        return dimensoesDobrado;
    }

    public void setDimensoesDobrado(String dimensoesDobrado) {
        this.dimensoesDobrado = dimensoesDobrado;
    }

    public String getDimensoesAberto() {
        return dimensoesAberto;
    }

    public void setDimensoesAberto(String dimensoesAberto) {
        this.dimensoesAberto = dimensoesAberto;
    }

    public Integer getAutonomiaMinutos() {
        return autonomiaMinutos;
    }

    public void setAutonomiaMinutos(Integer autonomiaMinutos) {
        this.autonomiaMinutos = autonomiaMinutos;
    }

    public Integer getAlcanceMetros() {
        return alcanceMetros;
    }

    public void setAlcanceMetros(Integer alcanceMetros) {
        this.alcanceMetros = alcanceMetros;
    }

    public Integer getVelocidadeMaximaKmh() {
        return velocidadeMaximaKmh;
    }

    public void setVelocidadeMaximaKmh(Integer velocidadeMaximaKmh) {
        this.velocidadeMaximaKmh = velocidadeMaximaKmh;
    }

    public Integer getResolucaoFotoMp() {
        return resolucaoFotoMp;
    }

    public void setResolucaoFotoMp(Integer resolucaoFotoMp) {
        this.resolucaoFotoMp = resolucaoFotoMp;
    }

    public String getTipoSensor() {
        return tipoSensor;
    }

    public void setTipoSensor(String tipoSensor) {
        this.tipoSensor = tipoSensor;
    }

    public String getResolucaoVideo() {
        return resolucaoVideo;
    }

    public void setResolucaoVideo(String resolucaoVideo) {
        this.resolucaoVideo = resolucaoVideo;
    }

    public String getEstabilizacao() {
        return estabilizacao;
    }

    public void setEstabilizacao(String estabilizacao) {
        this.estabilizacao = estabilizacao;
    }

    public String getTipoBateria() {
        return tipoBateria;
    }

    public void setTipoBateria(String tipoBateria) {
        this.tipoBateria = tipoBateria;
    }

    public Integer getCapacidadeBateriaMah() {
        return capacidadeBateriaMah;
    }

    public void setCapacidadeBateriaMah(Integer capacidadeBateriaMah) {
        this.capacidadeBateriaMah = capacidadeBateriaMah;
    }

    public Integer getTempoCarregamentoMinutos() {
        return tempoCarregamentoMinutos;
    }

    public void setTempoCarregamentoMinutos(Integer tempoCarregamentoMinutos) {
        this.tempoCarregamentoMinutos = tempoCarregamentoMinutos;
    }

    public boolean isGpsIntegrado() {
        return gpsIntegrado;
    }

    public void setGpsIntegrado(boolean gpsIntegrado) {
        this.gpsIntegrado = gpsIntegrado;
    }

    public boolean isSensoresAnticolisao() {
        return sensoresAnticolisao;
    }

    public void setSensoresAnticolisao(boolean sensoresAnticolisao) {
        this.sensoresAnticolisao = sensoresAnticolisao;
    }

    public boolean isExigeRegistroAnac() {
        return exigeRegistroAnac;
    }

    public void setExigeRegistroAnac(boolean exigeRegistroAnac) {
        this.exigeRegistroAnac = exigeRegistroAnac;
    }

    public String getClasseAnac() {
        return classeAnac;
    }

    public void setClasseAnac(String classeAnac) {
        this.classeAnac = classeAnac;
    }

    public String getRegistroSisant() {
        return registroSisant;
    }

    public void setRegistroSisant(String registroSisant) {
        this.registroSisant = registroSisant;
    }

}

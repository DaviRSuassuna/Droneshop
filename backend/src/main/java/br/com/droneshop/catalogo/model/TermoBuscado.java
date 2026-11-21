package br.com.droneshop.catalogo.model;

import java.time.LocalDateTime;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Termo buscado na loja, normalizado, com a contagem de buscas (#43, RF09). */
@Entity
@Table(name = "termo_buscado")
public class TermoBuscado extends DefaultEntity {

    @Column(nullable = false, unique = true, length = 200)
    private String termo;

    @Column(nullable = false)
    private int buscas;

    @Column(nullable = false, name = "ultima_quantidade_resultados")
    private int ultimaQuantidadeResultados;

    @Column(nullable = false, name = "ultima_busca")
    private LocalDateTime ultimaBusca;

    public String getTermo() { return termo; }
    public void setTermo(String termo) { this.termo = termo; }
    public int getBuscas() { return buscas; }
    public void setBuscas(int buscas) { this.buscas = buscas; }
    public int getUltimaQuantidadeResultados() { return ultimaQuantidadeResultados; }
    public void setUltimaQuantidadeResultados(int v) { this.ultimaQuantidadeResultados = v; }
    public LocalDateTime getUltimaBusca() { return ultimaBusca; }
    public void setUltimaBusca(LocalDateTime ultimaBusca) { this.ultimaBusca = ultimaBusca; }
}

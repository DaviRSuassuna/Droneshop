package br.com.droneshop.notificacao.model;

import java.util.List;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Opt-in de marketing por tipo (#42, RF41, RN10): sem registro, o tipo está desabilitado.
 * O token vai no link de descadastro dos e-mails daquele tipo.
 */
@Entity
@Table(name = "preferencia_notificacao",
        uniqueConstraints = @UniqueConstraint(columnNames = { "id_usuario", "tipoNotificacao" }))
public class PreferenciaNotificacao extends DefaultEntity {

    /** Tipos configuráveis; transacional sempre sai e não entra aqui. */
    public static final List<String> TIPOS = List.of("MARKETING", "CARRINHO_ABANDONADO", "NOVIDADES");

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 40)
    private String tipoNotificacao;

    @Column(nullable = false)
    private boolean habilitado;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenDescadastro;

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getTipoNotificacao() { return tipoNotificacao; }
    public void setTipoNotificacao(String tipoNotificacao) { this.tipoNotificacao = tipoNotificacao; }
    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
    public String getTokenDescadastro() { return tokenDescadastro; }
    public void setTokenDescadastro(String tokenDescadastro) { this.tokenDescadastro = tokenDescadastro; }
}

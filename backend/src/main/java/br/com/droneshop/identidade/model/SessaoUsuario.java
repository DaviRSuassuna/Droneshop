package br.com.droneshop.identidade.model;

import java.time.LocalDateTime;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Cada login abre uma sessão; o id vai no claim sid do JWT (RF50). */
@Entity
@Table(name = "sessao_usuario")
public class SessaoUsuario extends DefaultEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "perfil_ativo")
    private PerfilAtivo perfilAtivo;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "revogada_em")
    private LocalDateTime revogadaEm;

    public boolean valida() {
        return revogadaEm == null && expiraEm.isAfter(LocalDateTime.now());
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public PerfilAtivo getPerfilAtivo() {
        return perfilAtivo;
    }

    public void setPerfilAtivo(PerfilAtivo perfilAtivo) {
        this.perfilAtivo = perfilAtivo;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public LocalDateTime getRevogadaEm() {
        return revogadaEm;
    }

    public void setRevogadaEm(LocalDateTime revogadaEm) {
        this.revogadaEm = revogadaEm;
    }
}

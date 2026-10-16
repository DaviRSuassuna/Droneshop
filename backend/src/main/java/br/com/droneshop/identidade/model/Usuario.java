package br.com.droneshop.identidade.model;

import java.util.Locale;

import br.com.droneshop.administracao.model.PerfilAdministrativo;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario extends DefaultEntity {

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "senha_hash", length = 60)
    private String senhaHash;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado;

    @Column(nullable = false)
    private boolean ativo;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.EAGER)
    private PerfilAdministrativo perfilAdministrativo;

    @PrePersist
    @PreUpdate
    void normalizarEmail() {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public PerfilAdministrativo getPerfilAdministrativo() {
        return perfilAdministrativo;
    }

    public void setPerfilAdministrativo(PerfilAdministrativo perfilAdministrativo) {
        this.perfilAdministrativo = perfilAdministrativo;
    }
}

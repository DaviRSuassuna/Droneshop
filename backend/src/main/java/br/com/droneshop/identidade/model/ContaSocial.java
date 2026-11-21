package br.com.droneshop.identidade.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Vínculo do usuário com o provedor social; o identificador é o sub (D37). */
@Entity
@Table(name = "conta_social")
public class ContaSocial extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private ProvedorSocial provedor;

    @Column(name = "identificador_externo", nullable = false, length = 255)
    private String identificadorExterno;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ProvedorSocial getProvedor() {
        return provedor;
    }

    public void setProvedor(ProvedorSocial provedor) {
        this.provedor = provedor;
    }

    public String getIdentificadorExterno() {
        return identificadorExterno;
    }

    public void setIdentificadorExterno(String identificadorExterno) {
        this.identificadorExterno = identificadorExterno;
    }
}

package br.com.droneshop.administracao.model;

import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "perfil_administrativo")
public class PerfilAdministrativo extends DefaultEntity {

    @Column(nullable = false)
    private PapelAdministrativo papel;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PermissaoModulo> permissoes = new ArrayList<>();

    public PapelAdministrativo getPapel() {
        return papel;
    }

    public void setPapel(PapelAdministrativo papel) {
        this.papel = papel;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<PermissaoModulo> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(List<PermissaoModulo> permissoes) {
        this.permissoes = permissoes;
    }
}

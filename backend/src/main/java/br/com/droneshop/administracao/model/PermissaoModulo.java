package br.com.droneshop.administracao.model;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissao_modulo")
public class PermissaoModulo extends DefaultEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_administrativo_id", nullable = false)
    private PerfilAdministrativo perfil;

    @Column(nullable = false)
    private ModuloSistema modulo;

    @Column(name = "pode_ver", nullable = false)
    private boolean podeVer;

    @Column(name = "pode_editar", nullable = false)
    private boolean podeEditar;

    @Column(name = "pode_excluir", nullable = false)
    private boolean podeExcluir;

    public PermissaoModulo() {
    }

    public PermissaoModulo(PerfilAdministrativo perfil, ModuloSistema modulo, boolean podeVer, boolean podeEditar, boolean podeExcluir) {
        this.perfil = perfil;
        this.modulo = modulo;
        this.podeVer = podeVer;
        this.podeEditar = podeEditar;
        this.podeExcluir = podeExcluir;
    }

    public PerfilAdministrativo getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilAdministrativo perfil) {
        this.perfil = perfil;
    }

    public ModuloSistema getModulo() {
        return modulo;
    }

    public void setModulo(ModuloSistema modulo) {
        this.modulo = modulo;
    }

    public boolean isPodeVer() {
        return podeVer;
    }

    public void setPodeVer(boolean podeVer) {
        this.podeVer = podeVer;
    }

    public boolean isPodeEditar() {
        return podeEditar;
    }

    public void setPodeEditar(boolean podeEditar) {
        this.podeEditar = podeEditar;
    }

    public boolean isPodeExcluir() {
        return podeExcluir;
    }

    public void setPodeExcluir(boolean podeExcluir) {
        this.podeExcluir = podeExcluir;
    }
}

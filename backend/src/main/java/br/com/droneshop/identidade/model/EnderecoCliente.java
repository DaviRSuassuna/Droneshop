package br.com.droneshop.identidade.model;

import br.com.droneshop.endereco.model.Endereco;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Endereço do cliente: o endereço em si vem do módulo endereco (D32, município pelo IBGE).
// O pedido guarda uma cópia congelada (EnderecoPedido), então editar aqui não muda pedido (RN15).
@Entity
@Table(name = "endereco_cliente")
public class EnderecoCliente extends DefaultEntity {

    @Column(length = 40)
    private String apelido;

    @Column(nullable = false)
    private boolean principal;

    @Embedded
    private Endereco endereco;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_perfil_cliente", nullable = false)
    private PerfilCliente perfilCliente;

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public PerfilCliente getPerfilCliente() {
        return perfilCliente;
    }

    public void setPerfilCliente(PerfilCliente perfilCliente) {
        this.perfilCliente = perfilCliente;
    }
}

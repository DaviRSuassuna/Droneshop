package br.com.droneshop.pagamento.model;

import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Cartão salvo só pelo token do gateway; nunca o número completo (RN11). */
@Entity
@Table(name = "cartao_salvo")
public class CartaoSalvo extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_perfil_cliente", nullable = false)
    private PerfilCliente cliente;

    @Column(name = "token_gateway", nullable = false, length = 120)
    private String tokenGateway;

    @Column(name = "ultimos_digitos", nullable = false, length = 4)
    private String ultimosDigitos;

    @Column(length = 30)
    private String bandeira;

    @Column(length = 7)
    private String validade;

    @Column(nullable = false)
    private boolean principal;

    @Column(nullable = false)
    private boolean ativo;

    public PerfilCliente getCliente() {
        return cliente;
    }

    public void setCliente(PerfilCliente cliente) {
        this.cliente = cliente;
    }

    public String getTokenGateway() {
        return tokenGateway;
    }

    public void setTokenGateway(String tokenGateway) {
        this.tokenGateway = tokenGateway;
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    public void setUltimosDigitos(String ultimosDigitos) {
        this.ultimosDigitos = ultimosDigitos;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    public String getValidade() {
        return validade;
    }

    public void setValidade(String validade) {
        this.validade = validade;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}

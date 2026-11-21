package br.com.droneshop.compra.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Carrinho anônimo por token, vinculado ao cliente no login (RF16, RF17). */
@Entity
@Table(name = "carrinho")
public class Carrinho extends DefaultEntity {

    @Column(nullable = false)
    private StatusCarrinho status;

    @Column(name = "token_sessao", nullable = false, unique = true, length = 36)
    private String tokenSessao;

    @ManyToOne
    @JoinColumn(name = "id_perfil_cliente")
    private PerfilCliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_cupom")
    private Cupom cupomAplicado;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemCarrinho> itens = new ArrayList<>();

    @Column(name = "ultima_alteracao", nullable = false)
    private LocalDateTime ultimaAlteracao;

    public StatusCarrinho getStatus() {
        return status;
    }

    public void setStatus(StatusCarrinho status) {
        this.status = status;
    }

    public String getTokenSessao() {
        return tokenSessao;
    }

    public void setTokenSessao(String tokenSessao) {
        this.tokenSessao = tokenSessao;
    }

    public PerfilCliente getCliente() {
        return cliente;
    }

    public void setCliente(PerfilCliente cliente) {
        this.cliente = cliente;
    }

    public Cupom getCupomAplicado() {
        return cupomAplicado;
    }

    public void setCupomAplicado(Cupom cupomAplicado) {
        this.cupomAplicado = cupomAplicado;
    }

    public List<ItemCarrinho> getItens() {
        return itens;
    }

    public void setItens(List<ItemCarrinho> itens) {
        this.itens = itens;
    }

    public LocalDateTime getUltimaAlteracao() {
        return ultimaAlteracao;
    }

    public void setUltimaAlteracao(LocalDateTime ultimaAlteracao) {
        this.ultimaAlteracao = ultimaAlteracao;
    }
}

package br.com.droneshop.pagamento.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.pedido.model.Pedido;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Tentativa de pagamento do pedido; só uma é vigente (RF23). */
@Entity
@Table(name = "pagamento")
public class Pagamento extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(nullable = false)
    private MetodoPagamento metodo;

    @Column(nullable = false)
    private StatusPagamento status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private boolean vigente;

    @Column(name = "gateway_transacao_id", length = 60)
    private String gatewayTransacaoId;

    @ManyToOne
    @JoinColumn(name = "id_cartao_salvo")
    private CartaoSalvo cartao;

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Estorno> estornos = new ArrayList<>();

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPagamento metodo) {
        this.metodo = metodo;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }

    public String getGatewayTransacaoId() {
        return gatewayTransacaoId;
    }

    public void setGatewayTransacaoId(String gatewayTransacaoId) {
        this.gatewayTransacaoId = gatewayTransacaoId;
    }

    public CartaoSalvo getCartao() {
        return cartao;
    }

    public void setCartao(CartaoSalvo cartao) {
        this.cartao = cartao;
    }

    public List<Estorno> getEstornos() {
        return estornos;
    }

    public void setEstornos(List<Estorno> estornos) {
        this.estornos = estornos;
    }
}

package br.com.droneshop.pedido.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.compra.model.Carrinho;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.pagamento.model.MetodoPagamento;
import br.com.droneshop.pagamento.model.Pagamento;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

// O número exibido (AER-000001) vem do id, não é coluna. Criado pelo checkout a partir da #26;
// até lá só existe pela carga de dados (D19).
@Entity
@Table(name = "pedido")
public class Pedido extends DefaultEntity {

    @Column(nullable = false)
    private StatusPedido status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_perfil_cliente", nullable = false)
    private PerfilCliente cliente;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_frete", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorFrete;

    @Column(name = "valor_desconto", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorDesconto;

    @Embedded
    private EnderecoPedido enderecoEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemPedido> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataCadastro, id")
    private List<HistoricoStatusPedido> historico = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<DescontoPedido> descontos = new ArrayList<>();

    // Carrinho que gerou o pedido: liga o pedido às reservas dos itens (#26, #27)
    @ManyToOne
    @JoinColumn(name = "id_carrinho")
    private Carrinho carrinho;

    @Column(name = "metodo_pagamento")
    private MetodoPagamento metodoPagamento;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Entrega entrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Pagamento> pagamentos = new ArrayList<>();

    public Carrinho getCarrinho() {
        return carrinho;
    }

    public void setCarrinho(Carrinho carrinho) {
        this.carrinho = carrinho;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    /** Soma dos itens a preço cheio congelado; o total desconta descontos[] e soma o frete (RN16). */
    public BigDecimal getValorProdutos() {
        return itens.stream().map(i -> i.getPrecoUnitarioCongelado().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public PerfilCliente getCliente() {
        return cliente;
    }

    public void setCliente(PerfilCliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public EnderecoPedido getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(EnderecoPedido enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public List<HistoricoStatusPedido> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoStatusPedido> historico) {
        this.historico = historico;
    }

    public List<DescontoPedido> getDescontos() {
        return descontos;
    }

    public void setDescontos(List<DescontoPedido> descontos) {
        this.descontos = descontos;
    }
}

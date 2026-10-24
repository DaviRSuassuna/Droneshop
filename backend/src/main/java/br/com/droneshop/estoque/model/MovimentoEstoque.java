package br.com.droneshop.estoque.model;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.model.OrigemRegistro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Registro imutavel de cada alteracao do estoque fisico (RF14). Sem edicao nem exclusao:
// erro se corrige com outro movimento. A data do movimento e o dataCadastro.
@Entity
@Table(name = "movimento_estoque")
public class MovimentoEstoque extends DefaultEntity {

    // nunca kit: kit nao tem estoque proprio (D7)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private TipoMovimento tipo;

    @Column(nullable = false)
    private OrigemRegistro origem;

    // a quantidade informada; no AJUSTE, a diferenca entre o saldo novo e o anterior
    @Column(nullable = false)
    private int quantidade;

    @Column(name = "saldo_resultante", nullable = false)
    private int saldoResultante;

    @Column(length = 500)
    private String observacao;

    // D11: so quando a origem e MANUAL
    @ManyToOne
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public TipoMovimento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimento tipo) {
        this.tipo = tipo;
    }

    public OrigemRegistro getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemRegistro origem) {
        this.origem = origem;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public int getSaldoResultante() {
        return saldoResultante;
    }

    public void setSaldoResultante(int saldoResultante) {
        this.saldoResultante = saldoResultante;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

}

package br.com.droneshop.atendimento.model;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Avaliação do produto; uma por cliente e produto, publicada só depois de aprovada (#39, RN04, RN05). */
@Entity
@Table(name = "avaliacao", uniqueConstraints = @UniqueConstraint(columnNames = { "id_produto", "id_perfil_cliente" }))
public class Avaliacao extends DefaultEntity {

    @Column(nullable = false)
    private StatusModeracao status;

    @Column(nullable = false)
    private int nota;

    @Column(length = 2000)
    private String comentario;

    @Column(nullable = false)
    private boolean compraVerificada;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_perfil_cliente", nullable = false)
    private PerfilCliente cliente;

    public StatusModeracao getStatus() { return status; }
    public void setStatus(StatusModeracao status) { this.status = status; }
    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public boolean isCompraVerificada() { return compraVerificada; }
    public void setCompraVerificada(boolean compraVerificada) { this.compraVerificada = compraVerificada; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public PerfilCliente getCliente() { return cliente; }
    public void setCliente(PerfilCliente cliente) { this.cliente = cliente; }
}

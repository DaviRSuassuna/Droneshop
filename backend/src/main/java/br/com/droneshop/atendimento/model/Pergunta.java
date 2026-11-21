package br.com.droneshop.atendimento.model;

import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.catalogo.model.Produto;
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

/** Pergunta sobre o produto; só aparece na loja depois de aprovada (#40, RN05). */
@Entity
@Table(name = "pergunta")
public class Pergunta extends DefaultEntity {

    @Column(nullable = false)
    private StatusModeracao status;

    @Column(nullable = false, length = 1000)
    private String texto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_perfil_cliente", nullable = false)
    private PerfilCliente cliente;

    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<RespostaPergunta> respostas = new ArrayList<>();

    public StatusModeracao getStatus() { return status; }
    public void setStatus(StatusModeracao status) { this.status = status; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public PerfilCliente getCliente() { return cliente; }
    public void setCliente(PerfilCliente cliente) { this.cliente = cliente; }
    public List<RespostaPergunta> getRespostas() { return respostas; }
}

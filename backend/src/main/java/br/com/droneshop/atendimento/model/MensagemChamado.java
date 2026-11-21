package br.com.droneshop.atendimento.model;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Mensagem do histórico do chamado; nunca é editada (RF34). */
@Entity
@Table(name = "mensagem_chamado")
public class MensagemChamado extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_chamado", nullable = false)
    private Chamado chamado;

    @Column(nullable = false)
    private TipoAutorMensagem tipoAutor;

    @ManyToOne
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    @Column(nullable = false, columnDefinition = "text")
    private String texto;

    public Chamado getChamado() { return chamado; }
    public void setChamado(Chamado chamado) { this.chamado = chamado; }
    public TipoAutorMensagem getTipoAutor() { return tipoAutor; }
    public void setTipoAutor(TipoAutorMensagem tipoAutor) { this.tipoAutor = tipoAutor; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}

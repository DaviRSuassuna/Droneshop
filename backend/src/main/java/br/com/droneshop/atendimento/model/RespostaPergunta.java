package br.com.droneshop.atendimento.model;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Resposta da loja (oficial) ou de outro cliente (#40). */
@Entity
@Table(name = "resposta_pergunta")
public class RespostaPergunta extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pergunta", nullable = false)
    private Pergunta pergunta;

    @Column(nullable = false, length = 2000)
    private String texto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_autor", nullable = false)
    private Usuario autor;

    @Column(nullable = false)
    private boolean oficial;

    public Pergunta getPergunta() { return pergunta; }
    public void setPergunta(Pergunta pergunta) { this.pergunta = pergunta; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
    public boolean isOficial() { return oficial; }
    public void setOficial(boolean oficial) { this.oficial = oficial; }
}

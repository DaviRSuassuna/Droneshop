package br.com.droneshop.devolucao.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Estados da devolução (#37, D30). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusDevolucao {
    SOLICITADA(1L, "SOLICITADA"),
    EM_ANALISE(2L, "EM_ANALISE"),
    APROVADA(3L, "APROVADA"),
    RECUSADA(4L, "RECUSADA"),
    POSTAGEM_REVERSA(5L, "POSTAGEM_REVERSA"),
    RECEBIDA(6L, "RECEBIDA"),
    REEMBOLSADA(7L, "REEMBOLSADA"),
    CONCLUIDA(8L, "CONCLUIDA");

    private final Long id;
    private final String nome;

    StatusDevolucao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusDevolucao valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusDevolucao v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

package br.com.droneshop.estoque.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// ENTRADA e DEVOLUCAO somam; SAIDA e PERDA subtraem; AJUSTE informa o saldo novo
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoMovimento {
    ENTRADA(1L, "ENTRADA"),
    SAIDA(2L, "SAIDA"),
    AJUSTE(3L, "AJUSTE"),
    DEVOLUCAO(4L, "DEVOLUCAO"),
    PERDA(5L, "PERDA");

    private final Long id;
    private final String nome;

    TipoMovimento(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static TipoMovimento valueOf(Long id) {
        if (id == null)
            return null;
        for (TipoMovimento t : values()) {
            if (Objects.equals(t.getId(), id))
                return t;
        }
        return null;
    }
}

package br.com.droneshop.identidade.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoPessoa {
    PF(1L, "PF"),
    PJ(2L, "PJ");

    private final Long id;
    private final String nome;

    TipoPessoa(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static TipoPessoa valueOf(Long id) {
        if (id == null)
            return null;
        for (TipoPessoa t : values()) {
            if (Objects.equals(t.getId(), id))
                return t;
        }
        return null;
    }
}

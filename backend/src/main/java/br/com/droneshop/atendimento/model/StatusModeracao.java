package br.com.droneshop.atendimento.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusModeracao {
    PENDENTE(1L, "PENDENTE"),
    APROVADA(2L, "APROVADA"),
    REJEITADA(3L, "REJEITADA");

    private final Long id;
    private final String nome;

    StatusModeracao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusModeracao valueOf(Long id) {
        if (id == null) return null;
        for (StatusModeracao v : values()) {
            if (Objects.equals(v.getId(), id)) return v;
        }
        return null;
    }
}

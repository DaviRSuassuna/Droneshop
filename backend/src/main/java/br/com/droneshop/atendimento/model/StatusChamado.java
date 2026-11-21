package br.com.droneshop.atendimento.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusChamado {
    ABERTO(1L, "ABERTO"),
    EM_ANDAMENTO(2L, "EM_ANDAMENTO"),
    ENCERRADO(3L, "ENCERRADO");

    private final Long id;
    private final String nome;

    StatusChamado(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusChamado valueOf(Long id) {
        if (id == null) return null;
        for (StatusChamado v : values()) {
            if (Objects.equals(v.getId(), id)) return v;
        }
        return null;
    }
}

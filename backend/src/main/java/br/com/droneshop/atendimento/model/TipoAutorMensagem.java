package br.com.droneshop.atendimento.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoAutorMensagem {
    CLIENTE(1L, "CLIENTE"),
    ATENDENTE(2L, "ATENDENTE");

    private final Long id;
    private final String nome;

    TipoAutorMensagem(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static TipoAutorMensagem valueOf(Long id) {
        if (id == null) return null;
        for (TipoAutorMensagem v : values()) {
            if (Objects.equals(v.getId(), id)) return v;
        }
        return null;
    }
}

package br.com.droneshop.notificacao.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CategoriaNotificacao {
    TRANSACIONAL(1L, "TRANSACIONAL"),
    MARKETING(2L, "MARKETING");

    private final Long id;
    private final String nome;

    CategoriaNotificacao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static CategoriaNotificacao valueOf(Long id) {
        if (id == null) return null;
        for (CategoriaNotificacao v : values()) {
            if (Objects.equals(v.getId(), id)) return v;
        }
        return null;
    }
}

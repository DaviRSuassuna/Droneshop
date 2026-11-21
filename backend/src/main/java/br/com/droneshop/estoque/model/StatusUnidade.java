package br.com.droneshop.estoque.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Status da unidade individual (#34). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusUnidade {
    DISPONIVEL(1L, "DISPONIVEL"),
    RESERVADA(2L, "RESERVADA"),
    VENDIDA(3L, "VENDIDA"),
    DEVOLVIDA(4L, "DEVOLVIDA");

    private final Long id;
    private final String nome;

    StatusUnidade(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusUnidade valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusUnidade v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

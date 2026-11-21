package br.com.droneshop.estoque.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Estados da reserva (#23). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusReserva {
    ATIVA(1L, "ATIVA"),
    CONFIRMADA(2L, "CONFIRMADA"),
    EXPIRADA(3L, "EXPIRADA");

    private final Long id;
    private final String nome;

    StatusReserva(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusReserva valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusReserva v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

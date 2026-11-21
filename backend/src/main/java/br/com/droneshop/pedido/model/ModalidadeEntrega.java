package br.com.droneshop.pedido.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Modalidades de entrega (#30). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ModalidadeEntrega {
    EXPRESSA(1L, "EXPRESSA"),
    PADRAO(2L, "PADRAO"),
    RETIRADA_SHOWROOM(3L, "RETIRADA_SHOWROOM");

    private final Long id;
    private final String nome;

    ModalidadeEntrega(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static ModalidadeEntrega valueOf(Long id) {
        if (id == null)
            return null;
        for (ModalidadeEntrega v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

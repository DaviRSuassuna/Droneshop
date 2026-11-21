package br.com.droneshop.pedido.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Origem da NF-e simulada (#35). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoOrigemNota {
    VENDA(1L, "VENDA"),
    DEVOLUCAO(2L, "DEVOLUCAO");

    private final Long id;
    private final String nome;

    TipoOrigemNota(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static TipoOrigemNota valueOf(Long id) {
        if (id == null)
            return null;
        for (TipoOrigemNota v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

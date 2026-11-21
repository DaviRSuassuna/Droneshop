package br.com.droneshop.pedido.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** De onde veio o custo do frete (#30). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrigemCalculoFrete {
    INTEGRACAO_AGREGADOR(1L, "INTEGRACAO_AGREGADOR"),
    REGRA_FRETE(2L, "REGRA_FRETE");

    private final Long id;
    private final String nome;

    OrigemCalculoFrete(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static OrigemCalculoFrete valueOf(Long id) {
        if (id == null)
            return null;
        for (OrigemCalculoFrete v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

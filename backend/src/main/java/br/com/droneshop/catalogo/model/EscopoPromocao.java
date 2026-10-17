package br.com.droneshop.catalogo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum EscopoPromocao {
    PRODUTO(1L, "PRODUTO"),
    CARRINHO(2L, "CARRINHO"),
    FRETE(3L, "FRETE");

    private final Long id;
    private final String nome;

    EscopoPromocao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static EscopoPromocao valueOf(Long id) {
        if (id == null)
            return null;
        for (EscopoPromocao escopo : values()) {
            if (Objects.equals(escopo.getId(), id))
                return escopo;
        }
        return null;
    }
}

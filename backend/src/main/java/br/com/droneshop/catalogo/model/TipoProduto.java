package br.com.droneshop.catalogo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// O id e o valor da coluna discriminadora produto.tipo (ver Produto) e de categoria.tipo.
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoProduto {
    DRONE(1L, "DRONE"),
    ACESSORIO(2L, "ACESSORIO");

    private final Long ID;
    private final String NOME;

    TipoProduto(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static TipoProduto valueOf(Long id) {
        if (id == null)
            return null;
        for (TipoProduto t : TipoProduto.values()) {
            if (Objects.equals(t.getId(), id))
                return t;
        }
        return null;
    }
}

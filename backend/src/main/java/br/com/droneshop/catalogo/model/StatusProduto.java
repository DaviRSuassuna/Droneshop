package br.com.droneshop.catalogo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusProduto {
    RASCUNHO(1L, "Rascunho"),
    ATIVO(2L, "Ativo"),
    INATIVO(3L, "Inativo");

    private final Long ID;
    private final String NOME;

    StatusProduto(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static StatusProduto valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusProduto s : StatusProduto.values()) {
            if (Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }
}

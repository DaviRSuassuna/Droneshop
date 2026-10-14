package br.com.droneshop.catalogo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusFornecedor {
    ATIVO(1L, "Ativo"),
    INATIVO(2L, "Inativo");

    private final Long ID;
    private final String NOME;

    StatusFornecedor(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static StatusFornecedor valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusFornecedor s : StatusFornecedor.values()) {
            if (Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }
}

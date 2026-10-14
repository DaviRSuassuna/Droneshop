package br.com.droneshop.catalogo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusMarca {
    RASCUNHO(1L, "Rascunho"),
    ATIVA(2L, "Ativa"),
    INATIVA(3L, "Inativa");

    private final Long ID;
    private final String NOME;

    StatusMarca(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static StatusMarca valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusMarca s : StatusMarca.values()) {
            if (Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }
}

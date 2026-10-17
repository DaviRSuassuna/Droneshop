package br.com.droneshop.compra.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoDescontoCupom {
    PERCENTUAL(1L, "PERCENTUAL"),
    VALOR_FIXO(2L, "VALOR_FIXO");

    private final Long id;
    private final String nome;

    TipoDescontoCupom(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static TipoDescontoCupom valueOf(Long id) {
        if (id == null)
            return null;
        for (TipoDescontoCupom tipo : values()) {
            if (Objects.equals(tipo.getId(), id))
                return tipo;
        }
        return null;
    }
}

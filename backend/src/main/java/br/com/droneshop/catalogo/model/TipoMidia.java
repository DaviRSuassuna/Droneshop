package br.com.droneshop.catalogo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoMidia {
    IMAGEM(1L, "IMAGEM"),
    VIDEO(2L, "VIDEO"),
    MANUAL_PDF(3L, "MANUAL_PDF");

    private final Long id;
    private final String nome;

    TipoMidia(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static TipoMidia valueOf(Long id) {
        if (id == null)
            return null;
        for (TipoMidia tipo : values()) {
            if (Objects.equals(tipo.getId(), id))
                return tipo;
        }
        return null;
    }
}

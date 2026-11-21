package br.com.droneshop.identidade.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Provedores de login social (#47). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProvedorSocial {
    GOOGLE(1L, "GOOGLE");

    private final Long id;
    private final String nome;

    ProvedorSocial(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static ProvedorSocial valueOf(Long id) {
        if (id == null)
            return null;
        for (ProvedorSocial v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

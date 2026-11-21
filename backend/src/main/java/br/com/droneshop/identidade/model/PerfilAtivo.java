package br.com.droneshop.identidade.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Perfil com que a sessão entrou (RF48); vai no claim groups do JWT. */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PerfilAtivo {
    CLIENTE(1L, "CLIENTE"),
    ADMINISTRATIVO(2L, "ADMINISTRATIVO");

    private final Long id;
    private final String nome;

    PerfilAtivo(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static PerfilAtivo valueOf(Long id) {
        if (id == null)
            return null;
        for (PerfilAtivo p : values()) {
            if (Objects.equals(p.getId(), id))
                return p;
        }
        return null;
    }
}

package br.com.droneshop.administracao.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PapelAdministrativo {
    ADMIN(1L, "ADMIN"),
    OPERADOR(2L, "OPERADOR"),
    CATALOGO(3L, "CATALOGO"),
    ATENDIMENTO(4L, "ATENDIMENTO"),
    SOMENTE_LEITURA(5L, "SOMENTE_LEITURA");

    private final Long id;
    private final String nome;

    PapelAdministrativo(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static PapelAdministrativo valueOf(Long id) {
        if (id == null) return null;
        for (PapelAdministrativo papel : values()) {
            if (Objects.equals(papel.getId(), id)) return papel;
        }
        return null;
    }
}

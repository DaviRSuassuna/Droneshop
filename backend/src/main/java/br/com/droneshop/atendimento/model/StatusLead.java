package br.com.droneshop.atendimento.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusLead {
    NOVO(1L, "NOVO"),
    EM_CONTATO(2L, "EM_CONTATO"),
    FECHADO(3L, "FECHADO"),
    DESCARTADO(4L, "DESCARTADO");

    private final Long id;
    private final String nome;

    StatusLead(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusLead valueOf(Long id) {
        if (id == null) return null;
        for (StatusLead v : values()) {
            if (Objects.equals(v.getId(), id)) return v;
        }
        return null;
    }
}

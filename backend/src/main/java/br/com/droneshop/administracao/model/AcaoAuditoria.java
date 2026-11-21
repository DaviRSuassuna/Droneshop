package br.com.droneshop.administracao.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AcaoAuditoria {
    CRIACAO(1L, "CRIACAO"),
    ATUALIZACAO(2L, "ATUALIZACAO"),
    EXCLUSAO(3L, "EXCLUSAO"),
    LOGIN(4L, "LOGIN"),
    MUDANCA_STATUS(5L, "MUDANCA_STATUS");

    private final Long id;
    private final String nome;

    AcaoAuditoria(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static AcaoAuditoria valueOf(Long id) {
        if (id == null) return null;
        for (AcaoAuditoria v : values()) {
            if (Objects.equals(v.getId(), id)) return v;
        }
        return null;
    }
}

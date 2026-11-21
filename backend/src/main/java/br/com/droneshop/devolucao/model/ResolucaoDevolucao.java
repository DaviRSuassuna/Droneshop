package br.com.droneshop.devolucao.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Resolução pedida pelo cliente (#37). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResolucaoDevolucao {
    REEMBOLSO(1L, "REEMBOLSO"),
    TROCA(2L, "TROCA"),
    REPARO(3L, "REPARO");

    private final Long id;
    private final String nome;

    ResolucaoDevolucao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static ResolucaoDevolucao valueOf(Long id) {
        if (id == null)
            return null;
        for (ResolucaoDevolucao v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

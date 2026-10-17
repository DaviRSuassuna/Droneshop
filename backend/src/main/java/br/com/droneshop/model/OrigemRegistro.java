package br.com.droneshop.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// Compartilhado por histórico de pedido, movimento de estoque e histórico de devolução (D11)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrigemRegistro {
    MANUAL(1L, "MANUAL"),
    SISTEMA(2L, "SISTEMA"),
    INTEGRACAO(3L, "INTEGRACAO");

    private final Long id;
    private final String nome;

    OrigemRegistro(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static OrigemRegistro valueOf(Long id) {
        if (id == null)
            return null;
        for (OrigemRegistro o : values()) {
            if (Objects.equals(o.getId(), id))
                return o;
        }
        return null;
    }
}

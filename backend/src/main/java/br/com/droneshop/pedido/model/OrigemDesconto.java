package br.com.droneshop.pedido.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrigemDesconto {
    CUPOM(1L, "CUPOM"),
    PROMOCAO(2L, "PROMOCAO");

    private final Long id;
    private final String nome;

    OrigemDesconto(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static OrigemDesconto valueOf(Long id) {
        if (id == null)
            return null;
        for (OrigemDesconto origem : values()) {
            if (Objects.equals(origem.getId(), id))
                return origem;
        }
        return null;
    }
}

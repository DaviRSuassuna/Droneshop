package br.com.droneshop.compra.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Estados do carrinho (#24). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusCarrinho {
    ATIVO(1L, "ATIVO"),
    ABANDONADO(2L, "ABANDONADO"),
    CONVERTIDO(3L, "CONVERTIDO"),
    EXPIRADO(4L, "EXPIRADO");

    private final Long id;
    private final String nome;

    StatusCarrinho(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusCarrinho valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusCarrinho v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

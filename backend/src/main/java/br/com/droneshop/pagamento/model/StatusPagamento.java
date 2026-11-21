package br.com.droneshop.pagamento.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Status do pagamento (#27, #36). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusPagamento {
    PENDENTE(1L, "PENDENTE"),
    APROVADO(2L, "APROVADO"),
    RECUSADO(3L, "RECUSADO"),
    ESTORNADO(4L, "ESTORNADO"),
    ESTORNADO_PARCIAL(5L, "ESTORNADO_PARCIAL");

    private final Long id;
    private final String nome;

    StatusPagamento(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusPagamento valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusPagamento v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

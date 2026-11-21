package br.com.droneshop.pagamento.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Métodos de pagamento (#27). */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MetodoPagamento {
    PIX(1L, "PIX"),
    CARTAO(2L, "CARTAO"),
    BOLETO(3L, "BOLETO"),
    FATURAMENTO_PJ(4L, "FATURAMENTO_PJ");

    private final Long id;
    private final String nome;

    MetodoPagamento(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static MetodoPagamento valueOf(Long id) {
        if (id == null)
            return null;
        for (MetodoPagamento v : values()) {
            if (Objects.equals(v.getId(), id))
                return v;
        }
        return null;
    }
}

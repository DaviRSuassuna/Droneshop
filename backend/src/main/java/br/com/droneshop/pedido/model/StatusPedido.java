package br.com.droneshop.pedido.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusPedido {
    PENDENTE(1L, "PENDENTE"),
    PAGO(2L, "PAGO"),
    SEPARACAO(3L, "SEPARACAO"),
    ENVIADO(4L, "ENVIADO"),
    ENTREGUE(5L, "ENTREGUE"),
    EM_DEVOLUCAO(6L, "EM_DEVOLUCAO"),
    DEVOLVIDO(7L, "DEVOLVIDO"),
    CANCELADO(8L, "CANCELADO");

    private final Long id;
    private final String nome;

    StatusPedido(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusPedido valueOf(Long id) {
        if (id == null)
            return null;
        for (StatusPedido s : values()) {
            if (Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }
}

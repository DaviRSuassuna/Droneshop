package br.com.droneshop.notificacao.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusEnvioNotificacao {
    ENVIADA(1L, "ENVIADA"),
    ENTREGUE(2L, "ENTREGUE"),
    ABERTA(3L, "ABERTA"),
    BOUNCE(4L, "BOUNCE"),
    SPAM(5L, "SPAM"),
    FALHA(6L, "FALHA");

    private final Long id;
    private final String nome;

    StatusEnvioNotificacao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static StatusEnvioNotificacao valueOf(Long id) {
        if (id == null) return null;
        for (StatusEnvioNotificacao v : values()) {
            if (Objects.equals(v.getId(), id)) return v;
        }
        return null;
    }
}

package br.com.droneshop.administracao.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ModuloSistema {
    CATALOGO(1L, "CATALOGO"),
    ESTOQUE(2L, "ESTOQUE"),
    PEDIDO(3L, "PEDIDO"),
    PAGAMENTO(4L, "PAGAMENTO"),
    DEVOLUCAO(5L, "DEVOLUCAO"),
    ATENDIMENTO(6L, "ATENDIMENTO"),
    NOTIFICACAO(7L, "NOTIFICACAO"),
    ADMINISTRACAO(8L, "ADMINISTRACAO");

    private final Long id;
    private final String nome;

    ModuloSistema(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static ModuloSistema valueOf(Long id) {
        if (id == null) return null;
        for (ModuloSistema modulo : values()) {
            if (Objects.equals(modulo.getId(), id)) return modulo;
        }
        return null;
    }
}

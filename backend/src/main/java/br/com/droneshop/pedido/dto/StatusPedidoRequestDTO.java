package br.com.droneshop.pedido.dto;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.constraints.NotNull;

public record StatusPedidoRequestDTO(
    @NotNull(message = "O status deve ser informado (back).")
    Long idStatus,

    @Length(max = 500, message = "O campo deve ter no máximo 500 caracteres (back).")
    String observacao
) {
    public StatusPedidoRequestDTO {
        observacao = Textos.nuloSeVazio(observacao);
    }
}

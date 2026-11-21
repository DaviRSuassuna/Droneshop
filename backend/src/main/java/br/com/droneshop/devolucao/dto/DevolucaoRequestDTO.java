package br.com.droneshop.devolucao.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DevolucaoRequestDTO(
        @NotNull(message = "Informe o pedido (back)") Long idPedido,
        @NotNull(message = "Escolha a resolução (back)") Long idResolucao,
        @NotBlank(message = "Conte o motivo (back)") @Size(max = 500, message = "Use até 500 caracteres (back)") String motivo,
        List<Item> itens) {

    public record Item(Long idItemPedido, int quantidade) {
    }
}

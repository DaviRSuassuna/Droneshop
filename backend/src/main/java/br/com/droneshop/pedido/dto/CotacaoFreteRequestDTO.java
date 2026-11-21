package br.com.droneshop.pedido.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Sem itens, cota o carrinho do header X-Carrinho-Token. */
public record CotacaoFreteRequestDTO(String cep, @Valid List<Item> itens) {

    public record Item(@NotNull(message = "Informe o produto (back)") Long idProduto,
            @NotNull(message = "Informe a quantidade (back)") @Min(value = 1, message = "A quantidade mínima é 1 (back)") Integer quantidade) {
    }
}

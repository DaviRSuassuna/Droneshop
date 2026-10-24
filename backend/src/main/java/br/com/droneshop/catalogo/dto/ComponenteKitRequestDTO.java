package br.com.droneshop.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Item de "componentes" do kit (D7): qualquer produto que nao seja outro kit
public record ComponenteKitRequestDTO(
    @NotNull(message = "Escolha o produto componente (back).")
    Long idProduto,

    @NotNull(message = "Informe a quantidade (back).")
    @Min(value = 1, message = "A quantidade deve ser no mínimo 1 (back).")
    Integer quantidade
) {
}

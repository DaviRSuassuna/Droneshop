package br.com.droneshop.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Item de "componentes" na variante kit (D7)
public record ComponenteKitRequestDTO(
    @NotNull(message = "Escolha a variante componente (back).")
    Long idVariante,

    @NotNull(message = "Informe a quantidade (back).")
    @Min(value = 1, message = "A quantidade deve ser no mínimo 1 (back).")
    Integer quantidade
) {
}

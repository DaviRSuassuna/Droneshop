package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RegraFreteRequestDTO(
        @NotNull(message = "Informe a modalidade (back)") Long idModalidade,
        @NotNull(message = "Informe o custo (back)") @PositiveOrZero(message = "O custo não pode ser negativo (back)") BigDecimal custoFixo,
        @NotNull(message = "Informe o prazo (back)") @Min(value = 0, message = "O prazo não pode ser negativo (back)") Integer prazoDias,
        String cepOrigem) {
}

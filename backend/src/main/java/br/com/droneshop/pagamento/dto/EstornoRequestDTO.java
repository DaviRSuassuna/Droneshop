package br.com.droneshop.pagamento.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EstornoRequestDTO(
        @NotNull(message = "Informe o valor (back)") BigDecimal valor,
        @NotBlank(message = "Informe o motivo (back)") String motivo) {
}

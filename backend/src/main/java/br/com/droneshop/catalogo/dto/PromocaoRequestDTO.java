package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromocaoRequestDTO(
        @NotBlank(message = "O nome deve ser informado (back).")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres (back).")
        String nome,

        @NotNull(message = "O escopo deve ser informado (back).")
        Long idEscopo,

        @NotNull(message = "A data de início deve ser informada (back).")
        LocalDateTime dataInicio,

        @NotNull(message = "A data de fim deve ser informada (back).")
        LocalDateTime dataFim,

        @DecimalMin(value = "0.00", message = "O percentual deve estar entre 0 e 100 (back).")
        @DecimalMax(value = "100.00", message = "O percentual deve estar entre 0 e 100 (back).")
        BigDecimal percentualDesconto,

        @DecimalMin(value = "0.00", inclusive = false, message = "O valor do desconto deve ser maior que zero (back).")
        BigDecimal valorDesconto,

        @DecimalMin(value = "0.00", message = "O valor mínimo não pode ser negativo (back).")
        BigDecimal valorMinimoCarrinho,

        @NotNull(message = "A situação ativa deve ser informada (back).")
        Boolean ativa,

        List<Long> idsProdutos
) {
}

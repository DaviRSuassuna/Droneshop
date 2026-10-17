package br.com.droneshop.compra.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CupomRequestDTO(
        @NotNull(message = "O código deve ser informado (back).")
        @Size(min = 1, max = 40, message = "O código deve conter entre 1 e 40 caracteres (back).")
        @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Informe o código sem espaços (back).")
        String codigo,

        @NotNull(message = "O tipo de desconto deve ser informado (back).")
        Long idTipoDesconto,

        @NotNull(message = "O valor deve ser informado (back).")
        @DecimalMin(value = "0.00", message = "O valor do desconto não pode ser negativo (back).")
        BigDecimal valor,

        @NotNull(message = "A data de início deve ser informada (back).")
        LocalDateTime dataInicio,

        LocalDateTime dataFim,

        @DecimalMin(value = "0.00", message = "O valor mínimo não pode ser negativo (back).")
        BigDecimal valorMinimo,

        @Min(value = 1, message = "O limite total deve ser maior que zero (back).")
        Integer limiteTotalUso,

        @Min(value = 1, message = "O limite por cliente deve ser maior que zero (back).")
        Integer limitePorCliente,

        Boolean ativo,
        List<Long> idsCategorias,
        List<Long> idsMarcas,
        List<Long> idsVariantes
) {
}

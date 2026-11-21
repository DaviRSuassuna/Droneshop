package br.com.droneshop.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnidadeIndividualRequestDTO(
        @NotNull(message = "Informe o produto (back)") Long idProduto,
        @NotBlank(message = "Informe o número de série (back)") @Size(max = 60, message = "Use até 60 caracteres (back)") String numeroSerie,
        @Size(max = 40, message = "Use até 40 caracteres (back)") String registroAnac) {
}

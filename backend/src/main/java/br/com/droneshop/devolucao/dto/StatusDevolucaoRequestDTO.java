package br.com.droneshop.devolucao.dto;

import jakarta.validation.constraints.NotNull;

public record StatusDevolucaoRequestDTO(@NotNull(message = "Informe o status (back)") Long idStatus, String observacao) {
}

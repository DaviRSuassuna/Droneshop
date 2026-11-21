package br.com.droneshop.identidade.dto;

import jakarta.validation.constraints.NotNull;

public record PerfilAtivoRequestDTO(
        @NotNull(message = "Informe o perfil (back)") Long idPerfilAtivo) {
}

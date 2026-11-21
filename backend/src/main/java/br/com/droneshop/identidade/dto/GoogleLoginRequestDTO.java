package br.com.droneshop.identidade.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDTO(@NotBlank(message = "Informe o ID token (back)") String idToken) {
}

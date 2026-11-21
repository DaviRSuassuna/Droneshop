package br.com.droneshop.notificacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Entrada e saída de GET/PUT /conta/preferencias-notificacao e de GET /descadastro/{token} (#42). */
public record PreferenciaNotificacaoDTO(
        @NotBlank(message = "Informe o tipo de notificação (back)") String tipoNotificacao,
        @NotNull(message = "Informe se está habilitado (back)") Boolean habilitado) {
}

package br.com.droneshop.notificacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TemplateEmailRequestDTO(
        @NotBlank(message = "Informe o assunto (back)") @Size(max = 200, message = "Assunto com até 200 caracteres (back)") String assunto,
        @NotBlank(message = "Informe o corpo do template (back)") String corpoTemplate) {
}

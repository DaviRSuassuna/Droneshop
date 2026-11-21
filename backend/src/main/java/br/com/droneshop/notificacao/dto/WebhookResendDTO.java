package br.com.droneshop.notificacao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookResendDTO(String type, Dados data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dados(@JsonProperty("email_id") String emailId) {
    }
}

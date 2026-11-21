package br.com.droneshop.pagamento.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookMercadoPagoDTO(String type, Dados data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dados(String id) {
    }
}

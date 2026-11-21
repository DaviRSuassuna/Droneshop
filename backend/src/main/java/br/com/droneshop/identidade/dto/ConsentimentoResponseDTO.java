package br.com.droneshop.identidade.dto;

import java.time.LocalDateTime;

import br.com.droneshop.identidade.model.ConsentimentoLGPD;

public record ConsentimentoResponseDTO(String tipo, String versao, LocalDateTime dataConsentimento) {

    public static ConsentimentoResponseDTO de(ConsentimentoLGPD c) {
        return new ConsentimentoResponseDTO(c.getTipo(), c.getVersao(), c.getDataConsentimento());
    }
}

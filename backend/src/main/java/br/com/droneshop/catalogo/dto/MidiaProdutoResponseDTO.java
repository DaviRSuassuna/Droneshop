package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.TipoMidia;

public record MidiaProdutoResponseDTO(
        Long id,
        TipoMidia tipo,
        String url,
        boolean principal,
        int ordem,
        String altText) {
}

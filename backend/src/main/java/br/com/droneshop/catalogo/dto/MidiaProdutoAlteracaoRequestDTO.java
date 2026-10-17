package br.com.droneshop.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record MidiaProdutoAlteracaoRequestDTO(
        Boolean principal,
        @Min(1) Integer ordem,
        @Size(max = 160) String altText) {
}

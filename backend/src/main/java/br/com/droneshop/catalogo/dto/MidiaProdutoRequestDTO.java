package br.com.droneshop.catalogo.dto;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MidiaProdutoRequestDTO(
        @NotNull @Min(1) Long idTipo,
        FileUpload arquivo,
        @Size(max = 2048) String url,
        Boolean principal,
        @Min(1) Integer ordem,
        @Size(max = 160) String altText) {
}

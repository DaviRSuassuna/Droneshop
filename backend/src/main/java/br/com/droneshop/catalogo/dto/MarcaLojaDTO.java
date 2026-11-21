package br.com.droneshop.catalogo.dto;

public record MarcaLojaDTO(
        Long id,
        String nome,
        String slug,
        String logoUrl
) {
}

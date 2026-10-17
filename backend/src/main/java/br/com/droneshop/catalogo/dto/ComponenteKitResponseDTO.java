package br.com.droneshop.catalogo.dto;

public record ComponenteKitResponseDTO(
    VarianteListagemResponseDTO variante,
    int quantidade
) {
}

package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.StatusMarca;

public record MarcaResponseDTO(
    Long id,
    String nome,
    String slug,
    String paisOrigem,
    String siteOficial,
    String descricao,
    String logoUrl,
    StatusMarca status,
    boolean destacarHome
) {
}

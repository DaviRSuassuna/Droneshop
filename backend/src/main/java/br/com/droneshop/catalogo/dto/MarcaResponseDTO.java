package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.Marca;

public record MarcaResponseDTO(
    Long id,
    String nome,
    String slug,
    String paisOrigem,
    String siteOficial,
    String descricao,
    String logoUrl,
    String status,
    boolean destacarHome
) {
    public static MarcaResponseDTO valueOf(Marca marca) {
        return new MarcaResponseDTO(
            marca.getId(),
            marca.getNome(),
            marca.getSlug(),
            marca.getPaisOrigem(),
            marca.getSiteOficial(),
            marca.getDescricao(),
            marca.getLogoUrl(),
            marca.getStatus().name(),
            marca.isDestacarHome());
    }

}

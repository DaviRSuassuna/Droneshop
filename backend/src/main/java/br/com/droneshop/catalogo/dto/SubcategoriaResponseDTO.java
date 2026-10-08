package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.Subcategoria;

public record SubcategoriaResponseDTO(
    Long id,
    String nome,
    String slug,
    Long categoriaId,
    String categoriaNome,
    Integer ordemExibicao,
    String descricao,
    String status
) {
    public static SubcategoriaResponseDTO valueOf(Subcategoria subcategoria) {
        return new SubcategoriaResponseDTO(
            subcategoria.getId(),
            subcategoria.getNome(),
            subcategoria.getSlug(),
            subcategoria.getCategoria().getId(),
            subcategoria.getCategoria().getNome(),
            subcategoria.getOrdemExibicao(),
            subcategoria.getDescricao(),
            subcategoria.getStatus().name());
    }

}

package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.StatusCategoria;

public record SubcategoriaResponseDTO(
    Long id,
    String nome,
    String slug,
    CategoriaResponseDTO categoria,
    Integer ordemExibicao,
    String descricao,
    StatusCategoria status
) {
}

package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.Categoria;

public record CategoriaResponseDTO(
    Long id,
    String nome,
    String slug,
    Integer ordemExibicao,
    String descricao,
    String status,
    boolean exibirMenu,
    String iconeUrl
) {
    public static CategoriaResponseDTO valueOf(Categoria categoria) {
        return new CategoriaResponseDTO(
            categoria.getId(),
            categoria.getNome(),
            categoria.getSlug(),
            categoria.getOrdemExibicao(),
            categoria.getDescricao(),
            categoria.getStatus().name(),
            categoria.isExibirMenu(),
            categoria.getIconeUrl());
    }

}

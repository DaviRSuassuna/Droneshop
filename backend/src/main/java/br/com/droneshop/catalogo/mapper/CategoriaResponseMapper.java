package br.com.droneshop.catalogo.mapper;

import br.com.droneshop.catalogo.dto.CategoriaResponseDTO;
import br.com.droneshop.catalogo.model.Categoria;

public final class CategoriaResponseMapper {

    private CategoriaResponseMapper() {
    }

    public static CategoriaResponseDTO toResponse(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getSlug(),
                categoria.getOrdemExibicao(),
                categoria.getDescricao(),
                categoria.getStatus(),
                categoria.getTipo(),
                categoria.isExibirMenu(),
                categoria.getIconeUrl());
    }
}

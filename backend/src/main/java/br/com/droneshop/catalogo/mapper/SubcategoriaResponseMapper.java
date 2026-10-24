package br.com.droneshop.catalogo.mapper;

import br.com.droneshop.catalogo.dto.SubcategoriaResponseDTO;
import br.com.droneshop.catalogo.model.Subcategoria;

public final class SubcategoriaResponseMapper {

    private SubcategoriaResponseMapper() {
    }

    public static SubcategoriaResponseDTO toResponse(Subcategoria subcategoria) {
        return new SubcategoriaResponseDTO(
                subcategoria.getId(),
                subcategoria.getNome(),
                subcategoria.getSlug(),
                CategoriaResponseMapper.toResponse(subcategoria.getCategoria()),
                subcategoria.getOrdemExibicao(),
                subcategoria.getDescricao(),
                subcategoria.getStatus(),
                subcategoria.getQuantidadeProdutos());
    }
}

package br.com.droneshop.catalogo.dto;

import java.util.List;

import br.com.droneshop.catalogo.model.TipoProduto;

/** Categoria do menu da loja (#21): só ATIVA e exibirMenu, com as subcategorias ATIVA. */
public record CategoriaLojaDTO(
        Long id,
        String nome,
        String slug,
        TipoProduto tipo,
        List<Subcategoria> subcategorias
) {
    public record Subcategoria(Long id, String nome, String slug) {
    }
}

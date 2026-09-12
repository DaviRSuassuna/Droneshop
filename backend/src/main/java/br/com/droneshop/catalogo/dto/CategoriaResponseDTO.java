package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.model.StatusCategoria;

public record CategoriaResponseDTO(Long id, String nome, String slug, Long categoriaPaiId,
                                   String categoriaPaiNome, Integer ordemExibicao, String descricao,
                                   StatusCategoria status, boolean exibirMenu, String iconeUrl) {
    public static CategoriaResponseDTO valueOf(Categoria categoria) {
        Categoria pai = categoria.getCategoriaPai();
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNome(), categoria.getSlug(),
                pai == null ? null : pai.getId(), pai == null ? null : pai.getNome(),
                categoria.getOrdemExibicao(), categoria.getDescricao(), categoria.getStatus(),
                categoria.isExibirMenu(), categoria.getIconeUrl());
    }
}

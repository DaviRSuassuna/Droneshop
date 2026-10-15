package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.StatusCategoria;

public record CategoriaResponseDTO(
    Long id,
    String nome,
    String slug,
    Integer ordemExibicao,
    String descricao,
    StatusCategoria status,
    boolean exibirMenu,
    String iconeUrl
) {
}

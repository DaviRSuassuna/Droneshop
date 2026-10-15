package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.StatusCategoria;
import br.com.droneshop.catalogo.model.TipoProduto;

public record CategoriaResponseDTO(
    Long id,
    String nome,
    String slug,
    Integer ordemExibicao,
    String descricao,
    StatusCategoria status,
    TipoProduto tipo,
    boolean exibirMenu,
    String iconeUrl
) {
}

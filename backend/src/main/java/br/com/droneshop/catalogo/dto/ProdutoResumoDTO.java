package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.TipoProduto;

// Produto citado dentro de outro (compatibilidade, D17)
public record ProdutoResumoDTO(
    Long id,
    String nome,
    TipoProduto tipo,
    String imagemPrincipal
) {
}

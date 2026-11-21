package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

import br.com.droneshop.catalogo.model.TipoProduto;

/** Item da listagem da loja (#21): um por grupo (D40) ou por produto sem grupo. */
public record ProdutoLojaItemDTO(
        Long id,
        String nome,
        String slug,
        TipoProduto tipo,
        Marca marca,
        BigDecimal precoMinimo,
        String imagemPrincipal,
        boolean disponivel
) {
    public record Marca(String nome) {
    }
}

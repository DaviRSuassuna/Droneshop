package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

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
        boolean disponivel,
        // rótulos das versões do grupo (vazio quando o produto não tem grupo)
        List<String> versoes,
        // linha curta de especificações do card ("43 min · 15 km · 4K"); no acessório, a descrição curta
        String resumo,
        // preço cheio quando há promoção vigente; precoMinimo já vem com o desconto
        BigDecimal precoDe
) {
    public record Marca(String nome) {
    }
}

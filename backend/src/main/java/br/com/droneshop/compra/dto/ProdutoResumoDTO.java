package br.com.droneshop.compra.dto;

import java.math.BigDecimal;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;

/** Produto como aparece na lista de desejos e no carrinho. */
public record ProdutoResumoDTO(Long id, String nome, String sku, String slug, BigDecimal preco, boolean disponivel) {

    public static ProdutoResumoDTO de(Produto p) {
        return new ProdutoResumoDTO(p.getId(), p.getNome(), p.getSku(), p.getSlug(), p.getPreco(), disponivel(p));
    }

    /** À venda: ativo e com estoque (kit sem estoque próprio conta como disponível). */
    public static boolean disponivel(Produto p) {
        return p.getStatus() == StatusProduto.ATIVO && (p.getEstoqueFisico() == null || p.getEstoqueFisico() > 0);
    }
}

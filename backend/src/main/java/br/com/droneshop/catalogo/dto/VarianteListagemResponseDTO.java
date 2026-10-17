package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

import br.com.droneshop.catalogo.model.TipoProduto;

// Item de GET /variantes: o select de variante (estoque, promocao, kit) precisa saber de qual produto ela e
public record VarianteListagemResponseDTO(
    Long id,
    String sku,
    String nome,
    BigDecimal preco,
    Integer estoqueFisico,
    int disponivel,
    boolean ehKit,
    ProdutoResumo produto
) {
    public record ProdutoResumo(Long id, String nome, TipoProduto tipo) {
    }
}

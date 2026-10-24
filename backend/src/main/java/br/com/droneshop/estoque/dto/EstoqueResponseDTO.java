package br.com.droneshop.estoque.dto;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;

// Item de GET /estoque: saldo de um produto (D40). No kit (D7) estoqueFisico e nulo e o
// disponivel vem dos componentes; reservado e 0 ate a #23.
public record EstoqueResponseDTO(
    Produto produto,
    Integer estoqueFisico,
    int reservado,
    int disponivel,
    int estoqueMinimo,
    boolean abaixoDoMinimo
) {
    public record Produto(Long id, String sku, String nome, TipoProduto tipo, StatusProduto status) {
    }
}

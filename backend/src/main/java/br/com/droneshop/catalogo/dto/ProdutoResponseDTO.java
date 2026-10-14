package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

import br.com.droneshop.catalogo.model.StatusProduto;

public record ProdutoResponseDTO(
    Long id,
    String nome,
    String modelo,
    String descricaoCurta,
    BigDecimal preco,
    MarcaResponseDTO marca,
    SubcategoriaResponseDTO subcategoria,
    FornecedorResponseDTO fornecedor,
    StatusProduto status,
    boolean destaque
) {
}

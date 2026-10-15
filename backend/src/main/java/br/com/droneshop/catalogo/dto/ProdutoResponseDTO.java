package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.droneshop.catalogo.model.StatusProduto;

// marca, subcategoria e fornecedor vem null enquanto o produto for um rascunho incompleto
public record ProdutoResponseDTO(
    Long id,
    String nome,
    String modelo,
    String descricaoCurta,
    String descricao,
    String slug,
    String metaTitle,
    String metaDescription,
    String tags,
    int garantiaMeses,
    LocalDate dataLancamento,
    BigDecimal preco,
    MarcaResponseDTO marca,
    SubcategoriaResponseDTO subcategoria,
    FornecedorResponseDTO fornecedor,
    StatusProduto status,
    boolean destaque
) {
}

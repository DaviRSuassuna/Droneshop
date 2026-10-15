package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;

public record AcessorioResponseDTO(
    Long id,
    TipoProduto tipo,
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
    boolean destaque,
    String tipoAcessorio
) implements ProdutoResponseDTO {
}

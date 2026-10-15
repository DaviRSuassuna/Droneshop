package br.com.droneshop.catalogo.dto;

import java.time.LocalDate;
import java.util.List;

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
    MarcaResponseDTO marca,
    SubcategoriaResponseDTO subcategoria,
    FornecedorResponseDTO fornecedor,
    StatusProduto status,
    boolean destaque,
    String tipoAcessorio,
    List<VarianteResponseDTO> variantes
) implements ProdutoResponseDTO {
}

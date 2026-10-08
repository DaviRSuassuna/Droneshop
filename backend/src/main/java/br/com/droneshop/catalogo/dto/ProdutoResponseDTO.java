package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.Subcategoria;

// marca, subcategoria, categoria e fornecedor vao "achatados" (id + nome)
public record ProdutoResponseDTO(
    Long id,
    String nome,
    String modelo,
    String descricaoCurta,
    BigDecimal preco,
    Long marcaId,
    String marcaNome,
    Long subcategoriaId,
    String subcategoriaNome,
    Long categoriaId,
    String categoriaNome,
    Long fornecedorId,
    String fornecedorRazaoSocial,
    String status,
    boolean destaque
) {
    public static ProdutoResponseDTO valueOf(Produto produto) {
        Subcategoria subcategoria = produto.getSubcategoria();
        return new ProdutoResponseDTO(
            produto.getId(),
            produto.getNome(),
            produto.getModelo(),
            produto.getDescricaoCurta(),
            produto.getPreco(),
            produto.getMarca().getId(),
            produto.getMarca().getNome(),
            subcategoria.getId(),
            subcategoria.getNome(),
            subcategoria.getCategoria().getId(),
            subcategoria.getCategoria().getNome(),
            produto.getFornecedor().getId(),
            produto.getFornecedor().getRazaoSocial(),
            produto.getStatus().name(),
            produto.isDestaque());
    }

}

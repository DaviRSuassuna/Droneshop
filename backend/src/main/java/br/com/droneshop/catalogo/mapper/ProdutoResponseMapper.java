package br.com.droneshop.catalogo.mapper;

import br.com.droneshop.catalogo.dto.ProdutoResponseDTO;
import br.com.droneshop.catalogo.model.Produto;

public final class ProdutoResponseMapper {

    private ProdutoResponseMapper() {
    }

    public static ProdutoResponseDTO toResponse(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getModelo(),
                produto.getDescricaoCurta(),
                produto.getDescricao(),
                produto.getSlug(),
                produto.getMetaTitle(),
                produto.getMetaDescription(),
                produto.getTags(),
                produto.getGarantiaMeses(),
                produto.getDataLancamento(),
                produto.getPreco(),
                produto.getMarca() == null ? null : MarcaResponseMapper.toResponse(produto.getMarca()),
                produto.getSubcategoria() == null ? null : SubcategoriaResponseMapper.toResponse(produto.getSubcategoria()),
                produto.getFornecedor() == null ? null : FornecedorResponseMapper.toResponse(produto.getFornecedor()),
                produto.getStatus(),
                produto.isDestaque());
    }
}

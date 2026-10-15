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
                produto.getPreco(),
                MarcaResponseMapper.toResponse(produto.getMarca()),
                SubcategoriaResponseMapper.toResponse(produto.getSubcategoria()),
                FornecedorResponseMapper.toResponse(produto.getFornecedor()),
                produto.getStatus(),
                produto.isDestaque());
    }
}

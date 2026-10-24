package br.com.droneshop.catalogo.mapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

import br.com.droneshop.catalogo.dto.GrupoProdutoResponseDTO;
import br.com.droneshop.catalogo.model.GrupoProduto;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;

public final class GrupoProdutoResponseMapper {

    private GrupoProdutoResponseMapper() {
    }

    public static GrupoProdutoResponseDTO toResponse(GrupoProduto grupo) {
        BigDecimal aPartirDe = grupo.getProdutos().stream()
                .filter(p -> p.getStatus() == StatusProduto.ATIVO)
                .map(Produto::getPreco)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return new GrupoProdutoResponseDTO(
                grupo.getId(),
                grupo.getNome(),
                grupo.getSlug(),
                grupo.getDescricaoCurta(),
                grupo.isAtivo(),
                aPartirDe,
                grupo.getProdutos().stream()
                        .map(p -> new GrupoProdutoResponseDTO.Versao(p.getId(), p.getNome(), p.getSku(), p.getTipo(),
                                p.getStatus(), p.getRotuloNoGrupo(), p.getOrdemNoGrupo(), p.getPreco()))
                        .toList());
    }
}

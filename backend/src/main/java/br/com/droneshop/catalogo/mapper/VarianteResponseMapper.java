package br.com.droneshop.catalogo.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import br.com.droneshop.catalogo.dto.VarianteListagemResponseDTO;
import br.com.droneshop.catalogo.dto.VarianteResponseDTO;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.VarianteProduto;

public final class VarianteResponseMapper {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private VarianteResponseMapper() {
    }

    public static VarianteResponseDTO toResponse(VarianteProduto v) {
        return new VarianteResponseDTO(
                v.getId(),
                v.getNome(),
                v.getSku(),
                v.getPreco(),
                v.getCustoAquisicao(),
                margemPercentual(v.getPreco(), v.getCustoAquisicao()),
                v.getEstoqueFisico(),
                v.getEstoqueMinimo(),
                v.getPesoEmbalagemKg(),
                v.getAlturaEmbalagemCm(),
                v.getLarguraEmbalagemCm(),
                v.getComprimentoEmbalagemCm(),
                v.isEhKit(),
                v.isAtiva());
    }

    public static List<VarianteResponseDTO> toResponse(List<VarianteProduto> variantes) {
        return variantes.stream().map(VarianteResponseMapper::toResponse).toList();
    }

    public static VarianteListagemResponseDTO toListagem(VarianteProduto v) {
        Produto p = v.getProduto();
        return new VarianteListagemResponseDTO(
                v.getId(),
                v.getSku(),
                v.getNome(),
                v.getPreco(),
                v.getEstoqueFisico(),
                v.isEhKit(),
                new VarianteListagemResponseDTO.ProdutoResumo(p.getId(), p.getNome(), p.getTipo()));
    }

    // D26: (preco - custo) / preco * 100, com 2 casas; sem preco ou sem custo nao ha margem
    static BigDecimal margemPercentual(BigDecimal preco, BigDecimal custo) {
        if (preco == null || custo == null || preco.signum() <= 0)
            return null;
        return preco.subtract(custo).multiply(CEM).divide(preco, 2, RoundingMode.HALF_UP);
    }
}

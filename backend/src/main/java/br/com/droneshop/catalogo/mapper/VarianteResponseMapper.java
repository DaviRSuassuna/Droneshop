package br.com.droneshop.catalogo.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import br.com.droneshop.catalogo.dto.ComponenteKitResponseDTO;
import br.com.droneshop.catalogo.dto.VarianteListagemResponseDTO;
import br.com.droneshop.catalogo.dto.VarianteResponseDTO;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.catalogo.service.KitCalculo;

public final class VarianteResponseMapper {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private VarianteResponseMapper() {
    }

    public static VarianteResponseDTO toResponse(VarianteProduto v) {
        boolean kit = v.isEhKit();
        return new VarianteResponseDTO(
                v.getId(),
                v.getNome(),
                v.getSku(),
                v.getPreco(),
                v.getCustoAquisicao(),
                margemPercentual(v.getPreco(), v.getCustoAquisicao()),
                v.getEstoqueFisico(),
                KitCalculo.disponivel(v),
                v.getEstoqueMinimo(),
                KitCalculo.peso(v),
                KitCalculo.altura(v),
                KitCalculo.largura(v),
                KitCalculo.comprimento(v),
                kit && v.getPesoEmbalagemKg() != null,
                kit && (v.getAlturaEmbalagemCm() != null || v.getLarguraEmbalagemCm() != null
                        || v.getComprimentoEmbalagemCm() != null),
                kit,
                v.isAtiva(),
                v.getComponentes().stream()
                        .map(c -> new ComponenteKitResponseDTO(toListagem(c.getComponente()), c.getQuantidade()))
                        .toList());
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
                KitCalculo.disponivel(v),
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

package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

public record OfertaLojaResponseDTO(
        ProdutoLojaResumoDTO produto,
        BigDecimal precoDe,
        BigDecimal precoPor,
        PromocaoLojaResumoDTO promocao
) {
}

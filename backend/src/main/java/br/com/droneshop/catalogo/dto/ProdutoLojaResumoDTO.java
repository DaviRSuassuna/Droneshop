package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

public record ProdutoLojaResumoDTO(
        Long id,
        String nome,
        String slug,
        BigDecimal precoMinimo
) {
}

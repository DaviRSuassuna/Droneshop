package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

/** Facetas da listagem da loja (#21). */
public record FiltrosLojaDTO(
        List<Marca> marcas,
        Preco preco
) {
    public record Marca(String slug, String nome, long quantidade) {
    }

    public record Preco(BigDecimal min, BigDecimal max) {
    }
}

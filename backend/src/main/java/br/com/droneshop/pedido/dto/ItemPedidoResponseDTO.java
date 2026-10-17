package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;

public record ItemPedidoResponseDTO(
    Long id,
    VarianteResumo variante,
    ProdutoResumo produto,
    int quantidade,
    BigDecimal precoUnitarioCongelado,
    BigDecimal subtotal
) {
    public record VarianteResumo(Long id, String sku, String nome) {
    }

    public record ProdutoResumo(Long id, String nome) {
    }
}

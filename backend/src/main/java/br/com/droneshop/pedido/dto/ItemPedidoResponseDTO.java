package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;

public record ItemPedidoResponseDTO(
    Long id,
    ProdutoResumo produto,
    int quantidade,
    BigDecimal precoUnitarioCongelado,
    BigDecimal subtotal
) {
    public record ProdutoResumo(Long id, String sku, String nome) {
    }
}

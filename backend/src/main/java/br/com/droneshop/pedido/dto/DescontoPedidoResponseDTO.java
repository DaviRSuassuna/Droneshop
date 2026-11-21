package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;

import br.com.droneshop.pedido.model.DescontoPedido;
import br.com.droneshop.pedido.model.OrigemDesconto;

public record DescontoPedidoResponseDTO(OrigemDesconto origem, String descricao, BigDecimal valor) {

    public static DescontoPedidoResponseDTO de(DescontoPedido d) {
        return new DescontoPedidoResponseDTO(d.getOrigem(), d.getDescricao(), d.getValor());
    }
}

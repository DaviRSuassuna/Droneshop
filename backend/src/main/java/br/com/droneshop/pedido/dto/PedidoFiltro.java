package br.com.droneshop.pedido.dto;

import java.time.LocalDateTime;

import br.com.droneshop.pagamento.model.MetodoPagamento;
import br.com.droneshop.pedido.model.StatusPedido;

// Filtros de GET /pedidos já validados. Período: [inicio, fimExclusivo). idBusca vem do número do pedido,
// documento são os dígitos da busca quando ela parece um CPF ou CNPJ e metodo é o do pagamento vigente (#89).
public record PedidoFiltro(
    StatusPedido status,
    LocalDateTime inicio,
    LocalDateTime fimExclusivo,
    String nomeCliente,
    Long idBusca,
    String documento,
    MetodoPagamento metodo
) {
}

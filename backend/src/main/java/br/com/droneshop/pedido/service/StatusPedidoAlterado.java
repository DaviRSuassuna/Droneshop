package br.com.droneshop.pedido.service;

import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;

/**
 * Evento CDI síncrono disparado em toda mudança de status do pedido. Os módulos
 * reagem sem o pedido conhecer cada um: cupom (RN13), reservas, unidades (#34),
 * NF-e (#35). Um observador que lança exceção desfaz a mudança.
 */
public record StatusPedidoAlterado(Pedido pedido, StatusPedido anterior, StatusPedido novo) {
}

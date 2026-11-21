package br.com.droneshop.pedido.service;

import br.com.droneshop.pedido.model.Pedido;

/**
 * Evento CDI disparado quando o checkout cria o pedido. A notificação (#32) reage
 * depois do commit para mandar o e-mail de pedido confirmado.
 */
public record PedidoCriado(Pedido pedido) {
}

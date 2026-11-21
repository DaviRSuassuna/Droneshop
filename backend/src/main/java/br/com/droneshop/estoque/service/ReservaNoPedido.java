package br.com.droneshop.estoque.service;

import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.service.StatusPedidoAlterado;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/** Pedido CANCELADO antes do pagamento devolve o saldo reservado (#23). */
@ApplicationScoped
public class ReservaNoPedido {

    @Inject
    ReservaService reservas;

    void aoMudarStatus(@Observes StatusPedidoAlterado e) {
        if (e.novo() == StatusPedido.CANCELADO && e.pedido().getCarrinho() != null)
            reservas.liberar(e.pedido().getCarrinho().getItens());
    }
}

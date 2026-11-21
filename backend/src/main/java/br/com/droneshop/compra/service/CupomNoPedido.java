package br.com.droneshop.compra.service;

import br.com.droneshop.compra.repository.UsoCupomRepository;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.service.StatusPedidoAlterado;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/** Pedido CANCELADO libera o uso do cupom (RN13, #29). */
@ApplicationScoped
public class CupomNoPedido {

    @Inject
    UsoCupomRepository usos;

    void aoMudarStatus(@Observes StatusPedidoAlterado e) {
        if (e.novo() == StatusPedido.CANCELADO)
            usos.delete("pedido.id", e.pedido().getId());
    }
}

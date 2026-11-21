package br.com.droneshop.pedido.service;

import java.time.LocalDateTime;

import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.pedido.model.Entrega;
import br.com.droneshop.pedido.model.EventoRastreio;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** Código de rastreio e eventos da entrega (#31). */
@ApplicationScoped
public class RastreioService {

    @Inject
    PedidoRepository pedidos;

    public Pedido pedido(Long id) {
        Pedido p = pedidos.findById(id);
        if (p == null)
            throw new ResourceNotFoundException("Pedido não encontrado.");
        return p;
    }

    private static Entrega entrega(Pedido p) {
        if (p.getEntrega() == null)
            throw new ConflitoException("Este pedido não tem entrega.");
        return p.getEntrega();
    }

    @Transactional
    public void codigo(Long idPedido, String codigo) {
        entrega(pedido(idPedido)).setCodigoRastreio(codigo.trim().toUpperCase());
    }

    @Transactional
    public EventoRastreio evento(Long idPedido, String descricao, String localizacao, LocalDateTime dataHora) {
        Pedido p = pedido(idPedido);
        Entrega e = entrega(p);
        if (p.getStatus() != StatusPedido.ENVIADO && p.getStatus() != StatusPedido.ENTREGUE)
            throw new ConflitoException("O rastreio começa quando o pedido é enviado.");
        EventoRastreio ev = new EventoRastreio();
        ev.setEntrega(e);
        ev.setDescricao(descricao.trim());
        ev.setLocalizacao(localizacao == null || localizacao.isBlank() ? null : localizacao.trim());
        ev.setDataHora(dataHora == null ? LocalDateTime.now() : dataHora);
        e.getEventos().add(ev);
        pedidos.getEntityManager().persist(ev);
        return ev;
    }
}

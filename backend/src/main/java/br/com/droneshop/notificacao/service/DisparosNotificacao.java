package br.com.droneshop.notificacao.service;

import java.util.Map;

import br.com.droneshop.atendimento.model.Chamado;
import br.com.droneshop.atendimento.service.ChamadoRespondido;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.service.PedidoCriado;
import br.com.droneshop.pedido.service.StatusPedidoAlterado;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * Disparos transacionais do pedido (#32). Rodam depois do commit de quem disparou,
 * então uma falha no envio não desfaz o pedido (RNF05).
 */
@ApplicationScoped
public class DisparosNotificacao {

    @Inject
    NotificacaoService notificacoes;

    @Inject
    EntityManager em;

    void aoCriarPedido(@Observes(during = TransactionPhase.AFTER_SUCCESS) PedidoCriado e) {
        doPedido(e.pedido().getId(), "PEDIDO_CONFIRMADO");
    }

    void aoMudarStatus(@Observes(during = TransactionPhase.AFTER_SUCCESS) StatusPedidoAlterado e) {
        if (e.novo() == StatusPedido.PAGO)
            doPedido(e.pedido().getId(), "PAGAMENTO_APROVADO");
        else if (e.novo() == StatusPedido.ENVIADO)
            doPedido(e.pedido().getId(), "PEDIDO_ENVIADO");
    }

    void aoResponderChamado(@Observes(during = TransactionPhase.AFTER_SUCCESS) ChamadoRespondido e) {
        Object[] d = QuarkusTransaction.requiringNew().call(() -> {
            Chamado c = em.find(Chamado.class, e.idChamado());
            Usuario u = c.getCliente().getUsuario();
            return new Object[] { u.getId(), u.getEmail(), c.getCliente().getNome(), c.getProtocolo(), c.getAssunto() };
        });
        notificacoes.enviar("CHAMADO_RESPONDIDO", (String) d[1], (Long) d[0], Map.of(
                "nome", d[2] == null ? "" : (String) d[2],
                "protocolo", (String) d[3],
                "assunto", (String) d[4]));
    }

    private void doPedido(Long idPedido, String codigo) {
        Object[] d = QuarkusTransaction.requiringNew().call(() -> {
            Pedido p = em.find(Pedido.class, idPedido);
            Usuario u = p.getCliente().getUsuario();
            return new Object[] { u.getId(), u.getEmail(), p.getCliente().getNome(), p.getValorTotal() };
        });
        notificacoes.enviar(codigo, (String) d[1], (Long) d[0], Map.of(
                "nome", d[2] == null ? "" : (String) d[2],
                "numeroPedido", String.valueOf(idPedido),
                "valorTotal", String.valueOf(d[3])));
    }
}

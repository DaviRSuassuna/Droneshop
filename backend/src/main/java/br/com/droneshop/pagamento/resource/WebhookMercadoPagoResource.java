package br.com.droneshop.pagamento.resource;

import br.com.droneshop.pagamento.dto.WebhookMercadoPagoDTO;
import br.com.droneshop.pagamento.service.PagamentoService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Notificação do Mercado Pago (público). O corpo só diz qual pagamento mudou: o status
 * sempre vem de uma consulta ao gateway, nunca do corpo (#27).
 */
@Path("webhooks/mercadopago")
@PermitAll
public class WebhookMercadoPagoResource {

    @Inject
    PagamentoService pagamentos;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receber(WebhookMercadoPagoDTO dto) {
        if (dto != null && "payment".equals(dto.type()) && dto.data() != null)
            pagamentos.webhook(dto.data().id());
        return Response.ok().build();
    }
}

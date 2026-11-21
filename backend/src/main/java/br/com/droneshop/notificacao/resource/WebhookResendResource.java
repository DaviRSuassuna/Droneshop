package br.com.droneshop.notificacao.resource;

import br.com.droneshop.notificacao.dto.WebhookResendDTO;
import br.com.droneshop.notificacao.service.NotificacaoService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Webhook do Resend: atualiza o status de entrega da notificação (RF40). */
@Path("/webhooks/resend")
@PermitAll
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebhookResendResource {

    @Inject
    NotificacaoService service;

    @POST
    public java.util.Map<String, Boolean> receber(WebhookResendDTO evento) {
        service.webhook(evento);
        return java.util.Map.of("recebido", true);
    }
}

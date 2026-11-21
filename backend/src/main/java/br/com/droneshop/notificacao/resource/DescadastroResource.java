package br.com.droneshop.notificacao.resource;

import br.com.droneshop.notificacao.dto.PreferenciaNotificacaoDTO;
import br.com.droneshop.notificacao.service.PreferenciaNotificacaoService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Link de descadastro dos e-mails de marketing, sem login (#42, RN10). */
@Path("descadastro")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
public class DescadastroResource {

    @Inject
    PreferenciaNotificacaoService service;

    @GET
    @Path("{token}")
    public PreferenciaNotificacaoDTO descadastrar(@PathParam("token") String token) {
        return service.descadastrar(token);
    }
}

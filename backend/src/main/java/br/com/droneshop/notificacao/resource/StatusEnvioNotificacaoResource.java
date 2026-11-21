package br.com.droneshop.notificacao.resource;

import br.com.droneshop.notificacao.model.StatusEnvioNotificacao;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Rota de leitura do enum (D31). */
@Path("status-envio-notificacao")
@Produces(MediaType.APPLICATION_JSON)
public class StatusEnvioNotificacaoResource {

    @GET
    public StatusEnvioNotificacao[] buscarTodos() {
        return StatusEnvioNotificacao.values();
    }

    @GET
    @Path("/{id}")
    public StatusEnvioNotificacao buscarPorId(@PathParam("id") Long id) {
        StatusEnvioNotificacao v = StatusEnvioNotificacao.valueOf(id);
        if (v == null) throw new ResourceNotFoundException("Valor não encontrado.");
        return v;
    }
}

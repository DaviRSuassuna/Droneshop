package br.com.droneshop.atendimento.resource;

import br.com.droneshop.atendimento.model.StatusModeracao;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Rota de leitura do enum (D31). */
@PermitAll
@Path("status-moderacao")
@Produces(MediaType.APPLICATION_JSON)
public class StatusModeracaoResource {

    @GET
    public StatusModeracao[] buscarTodos() {
        return StatusModeracao.values();
    }

    @GET
    @Path("/{id}")
    public StatusModeracao buscarPorId(@PathParam("id") Long id) {
        StatusModeracao v = StatusModeracao.valueOf(id);
        if (v == null) throw new ResourceNotFoundException("Valor não encontrado.");
        return v;
    }
}

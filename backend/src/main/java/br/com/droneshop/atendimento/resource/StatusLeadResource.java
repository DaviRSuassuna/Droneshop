package br.com.droneshop.atendimento.resource;

import br.com.droneshop.atendimento.model.StatusLead;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Rota de leitura do enum (D31). */
@PermitAll
@Path("status-lead")
@Produces(MediaType.APPLICATION_JSON)
public class StatusLeadResource {

    @GET
    public StatusLead[] buscarTodos() {
        return StatusLead.values();
    }

    @GET
    @Path("/{id}")
    public StatusLead buscarPorId(@PathParam("id") Long id) {
        StatusLead v = StatusLead.valueOf(id);
        if (v == null) throw new ResourceNotFoundException("Valor não encontrado.");
        return v;
    }
}

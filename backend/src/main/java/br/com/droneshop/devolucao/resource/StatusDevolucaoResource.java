package br.com.droneshop.devolucao.resource;

import br.com.droneshop.devolucao.model.StatusDevolucao;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@PermitAll
@Path("status-devolucao")
@Produces(MediaType.APPLICATION_JSON)
public class StatusDevolucaoResource {

    @GET
    public StatusDevolucao[] buscarTodos() {
        return StatusDevolucao.values();
    }
}

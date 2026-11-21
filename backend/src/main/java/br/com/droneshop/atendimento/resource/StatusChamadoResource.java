package br.com.droneshop.atendimento.resource;

import br.com.droneshop.atendimento.model.StatusChamado;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Rota de leitura do enum (D31). */
@Path("status-chamado")
@Produces(MediaType.APPLICATION_JSON)
public class StatusChamadoResource {

    @GET
    public StatusChamado[] buscarTodos() {
        return StatusChamado.values();
    }

    @GET
    @Path("/{id}")
    public StatusChamado buscarPorId(@PathParam("id") Long id) {
        StatusChamado v = StatusChamado.valueOf(id);
        if (v == null) throw new ResourceNotFoundException("Valor não encontrado.");
        return v;
    }
}

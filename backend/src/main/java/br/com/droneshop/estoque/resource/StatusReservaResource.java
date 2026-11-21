package br.com.droneshop.estoque.resource;

import br.com.droneshop.estoque.model.StatusReserva;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@Path("status-reserva")
@Produces(MediaType.APPLICATION_JSON)
public class StatusReservaResource {

    @GET
    public StatusReserva[] buscarTodos() {
        return StatusReserva.values();
    }
}

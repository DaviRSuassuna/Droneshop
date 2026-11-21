package br.com.droneshop.estoque.resource;

import br.com.droneshop.estoque.model.StatusUnidade;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@Path("status-unidade")
@Produces(MediaType.APPLICATION_JSON)
public class StatusUnidadeResource {

    @GET
    public StatusUnidade[] buscarTodos() {
        return StatusUnidade.values();
    }
}

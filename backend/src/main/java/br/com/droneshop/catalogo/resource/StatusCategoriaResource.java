package br.com.droneshop.catalogo.resource;

import br.com.droneshop.catalogo.model.StatusCategoria;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("status-categoria")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatusCategoriaResource {

    @GET
    public StatusCategoria[] buscarTodos() {
        return StatusCategoria.values();
    }

    @GET
    @Path("/{id}")
    public StatusCategoria buscarPorId(@PathParam("id") Long id) {
        StatusCategoria status = StatusCategoria.valueOf(id);
        if (status == null)
            throw new ResourceNotFoundException("Status não encontrado.");
        return status;
    }

}

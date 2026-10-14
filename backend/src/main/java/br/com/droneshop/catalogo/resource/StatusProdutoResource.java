package br.com.droneshop.catalogo.resource;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("status-produto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatusProdutoResource {

    @GET
    public StatusProduto[] buscarTodos() {
        return StatusProduto.values();
    }

    @GET
    @Path("/{id}")
    public StatusProduto buscarPorId(@PathParam("id") Long id) {
        StatusProduto status = StatusProduto.valueOf(id);
        if (status == null)
            throw new ResourceNotFoundException("Status não encontrado.");
        return status;
    }

}

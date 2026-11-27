package br.com.droneshop.pedido.resource;

import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.pedido.model.StatusPedido;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@PermitAll
@Path("status-pedido")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatusPedidoResource {

    @GET
    public StatusPedido[] buscarTodos() {
        return StatusPedido.values();
    }

    @GET
    @Path("/{id}")
    public StatusPedido buscarPorId(@PathParam("id") Long id) {
        StatusPedido status = StatusPedido.valueOf(id);
        if (status == null)
            throw new ResourceNotFoundException("Status não encontrado.");
        return status;
    }

}

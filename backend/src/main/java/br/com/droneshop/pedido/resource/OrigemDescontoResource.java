package br.com.droneshop.pedido.resource;

import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.pedido.model.OrigemDesconto;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@PermitAll
@Path("origens-desconto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrigemDescontoResource {

    @GET
    public OrigemDesconto[] buscarTodos() {
        return OrigemDesconto.values();
    }

    @GET
    @Path("/{id}")
    public OrigemDesconto buscarPorId(@PathParam("id") Long id) {
        OrigemDesconto origem = OrigemDesconto.valueOf(id);
        if (origem == null)
            throw new ResourceNotFoundException("Origem de desconto não encontrada.");
        return origem;
    }
}

package br.com.droneshop.compra.resource;

import br.com.droneshop.compra.model.TipoDescontoCupom;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@PermitAll
@Path("tipos-desconto-cupom")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoDescontoCupomResource {
    @GET
    public TipoDescontoCupom[] buscarTodos() {
        return TipoDescontoCupom.values();
    }

    @GET
    @Path("/{id}")
    public TipoDescontoCupom buscarPorId(@PathParam("id") Long id) {
        TipoDescontoCupom tipo = TipoDescontoCupom.valueOf(id);
        if (tipo == null)
            throw new ResourceNotFoundException("Tipo de desconto de cupom não encontrado.");
        return tipo;
    }
}

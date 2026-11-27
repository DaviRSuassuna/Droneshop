package br.com.droneshop.resource;

import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.model.OrigemRegistro;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Enum compartilhado (D11), por isso fica fora dos modulos
@PermitAll
@Path("origens-registro")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrigemRegistroResource {

    @GET
    public OrigemRegistro[] buscarTodos() {
        return OrigemRegistro.values();
    }

    @GET
    @Path("/{id}")
    public OrigemRegistro buscarPorId(@PathParam("id") Long id) {
        OrigemRegistro origem = OrigemRegistro.valueOf(id);
        if (origem == null)
            throw new ResourceNotFoundException("Origem não encontrada.");
        return origem;
    }

}

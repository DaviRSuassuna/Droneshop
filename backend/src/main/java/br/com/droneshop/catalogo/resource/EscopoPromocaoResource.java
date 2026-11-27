package br.com.droneshop.catalogo.resource;

import br.com.droneshop.catalogo.model.EscopoPromocao;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@PermitAll
@Path("escopos-promocao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EscopoPromocaoResource {

    @GET
    public EscopoPromocao[] buscarTodos() {
        return EscopoPromocao.values();
    }

    @GET
    @Path("/{id}")
    public EscopoPromocao buscarPorId(@PathParam("id") Long id) {
        EscopoPromocao escopo = EscopoPromocao.valueOf(id);
        if (escopo == null)
            throw new ResourceNotFoundException("Escopo de promoção não encontrado.");
        return escopo;
    }
}

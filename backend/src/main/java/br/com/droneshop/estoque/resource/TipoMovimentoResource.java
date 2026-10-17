package br.com.droneshop.estoque.resource;

import br.com.droneshop.estoque.model.TipoMovimento;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("tipos-movimento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoMovimentoResource {

    @GET
    public TipoMovimento[] buscarTodos() {
        return TipoMovimento.values();
    }

    @GET
    @Path("/{id}")
    public TipoMovimento buscarPorId(@PathParam("id") Long id) {
        TipoMovimento tipo = TipoMovimento.valueOf(id);
        if (tipo == null)
            throw new ResourceNotFoundException("Tipo de movimento não encontrado.");
        return tipo;
    }

}

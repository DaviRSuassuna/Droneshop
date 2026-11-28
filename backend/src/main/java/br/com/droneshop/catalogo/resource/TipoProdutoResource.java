package br.com.droneshop.catalogo.resource;

import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@PermitAll
@Path("tipos-produto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoProdutoResource {

    @GET
    public TipoProduto[] buscarTodos() {
        return TipoProduto.values();
    }

    @GET
    @Path("/{id}")
    public TipoProduto buscarPorId(@PathParam("id") Long id) {
        TipoProduto tipo = TipoProduto.valueOf(id);
        if (tipo == null)
            throw new ResourceNotFoundException("Tipo de produto não encontrado.");
        return tipo;
    }

}

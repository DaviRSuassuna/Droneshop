package br.com.droneshop.pedido.resource;

import br.com.droneshop.pedido.model.OrigemCalculoFrete;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@Path("origens-calculo-frete")
@Produces(MediaType.APPLICATION_JSON)
public class OrigemCalculoFreteResource {

    @GET
    public OrigemCalculoFrete[] buscarTodos() {
        return OrigemCalculoFrete.values();
    }
}

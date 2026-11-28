package br.com.droneshop.compra.resource;

import br.com.droneshop.compra.model.StatusCarrinho;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@PermitAll
@Path("status-carrinho")
@Produces(MediaType.APPLICATION_JSON)
public class StatusCarrinhoResource {

    @GET
    public StatusCarrinho[] buscarTodos() {
        return StatusCarrinho.values();
    }
}

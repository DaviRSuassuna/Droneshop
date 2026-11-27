package br.com.droneshop.pedido.resource;

import br.com.droneshop.pedido.model.ModalidadeEntrega;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@PermitAll
@Path("modalidades-entrega")
@Produces(MediaType.APPLICATION_JSON)
public class ModalidadeEntregaResource {

    @GET
    public ModalidadeEntrega[] buscarTodos() {
        return ModalidadeEntrega.values();
    }
}

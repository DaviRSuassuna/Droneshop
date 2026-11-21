package br.com.droneshop.pedido.resource;

import br.com.droneshop.pedido.model.TipoOrigemNota;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@Path("tipos-origem-nota")
@Produces(MediaType.APPLICATION_JSON)
public class TipoOrigemNotaResource {

    @GET
    public TipoOrigemNota[] buscarTodos() {
        return TipoOrigemNota.values();
    }
}

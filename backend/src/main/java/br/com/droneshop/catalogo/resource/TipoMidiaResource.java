package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.model.TipoMidia;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("tipos-midia")
@Produces(MediaType.APPLICATION_JSON)
public class TipoMidiaResource {

    @GET
    public List<TipoMidia> buscarTodos() {
        return List.of(TipoMidia.values());
    }
}

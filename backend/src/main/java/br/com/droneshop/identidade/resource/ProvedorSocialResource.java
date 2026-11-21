package br.com.droneshop.identidade.resource;

import br.com.droneshop.identidade.model.ProvedorSocial;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@Path("provedores-social")
@Produces(MediaType.APPLICATION_JSON)
public class ProvedorSocialResource {

    @GET
    public ProvedorSocial[] buscarTodos() {
        return ProvedorSocial.values();
    }
}

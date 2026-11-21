package br.com.droneshop.identidade.resource;

import br.com.droneshop.identidade.model.PerfilAtivo;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Público: a escolha de perfil no login precisa da lista (D31).
@Path("perfis-ativos")
@Produces(MediaType.APPLICATION_JSON)
public class PerfilAtivoResource {

    @GET
    public PerfilAtivo[] buscarTodos() {
        return PerfilAtivo.values();
    }
}

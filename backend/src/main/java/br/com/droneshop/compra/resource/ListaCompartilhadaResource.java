package br.com.droneshop.compra.resource;

import br.com.droneshop.compra.dto.ListaCompartilhadaResponseDTO;
import br.com.droneshop.compra.service.FavoritosService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Lista de favoritos compartilhada, pública e só de leitura (#86). */
@Path("loja/listas/{token}")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
public class ListaCompartilhadaResource {

    @Inject
    FavoritosService favoritos;

    @GET
    public ListaCompartilhadaResponseDTO buscar(@PathParam("token") String token) {
        return favoritos.publica(token);
    }
}

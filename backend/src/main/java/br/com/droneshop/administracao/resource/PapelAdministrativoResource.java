package br.com.droneshop.administracao.resource;

import br.com.droneshop.administracao.model.PapelAdministrativo;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("papeis-administrativos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PapelAdministrativoResource {

    @GET
    public PapelAdministrativo[] buscarTodos() {
        return PapelAdministrativo.values();
    }

    @GET
    @Path("/{id}")
    public PapelAdministrativo buscarPorId(@PathParam("id") Long id) {
        PapelAdministrativo papel = PapelAdministrativo.valueOf(id);
        if (papel == null) {
            throw new ResourceNotFoundException("Papel não encontrado.");
        }
        return papel;
    }
}

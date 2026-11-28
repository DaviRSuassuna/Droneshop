package br.com.droneshop.administracao.resource;

import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@PermitAll
@Path("modulos-sistema")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModuloSistemaResource {

    @GET
    public ModuloSistema[] buscarTodos() {
        return ModuloSistema.values();
    }

    @GET
    @Path("/{id}")
    public ModuloSistema buscarPorId(@PathParam("id") Long id) {
        ModuloSistema modulo = ModuloSistema.valueOf(id);
        if (modulo == null) {
            throw new ResourceNotFoundException("Módulo não encontrado.");
        }
        return modulo;
    }
}

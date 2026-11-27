package br.com.droneshop.notificacao.resource;

import br.com.droneshop.notificacao.model.CategoriaNotificacao;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Rota de leitura do enum (D31). */
@PermitAll
@Path("categorias-notificacao")
@Produces(MediaType.APPLICATION_JSON)
public class CategoriaNotificacaoResource {

    @GET
    public CategoriaNotificacao[] buscarTodos() {
        return CategoriaNotificacao.values();
    }

    @GET
    @Path("/{id}")
    public CategoriaNotificacao buscarPorId(@PathParam("id") Long id) {
        CategoriaNotificacao v = CategoriaNotificacao.valueOf(id);
        if (v == null) throw new ResourceNotFoundException("Valor não encontrado.");
        return v;
    }
}

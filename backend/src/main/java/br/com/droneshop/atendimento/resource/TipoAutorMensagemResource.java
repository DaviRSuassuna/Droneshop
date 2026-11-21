package br.com.droneshop.atendimento.resource;

import br.com.droneshop.atendimento.model.TipoAutorMensagem;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Rota de leitura do enum (D31). */
@Path("tipos-autor-mensagem")
@Produces(MediaType.APPLICATION_JSON)
public class TipoAutorMensagemResource {

    @GET
    public TipoAutorMensagem[] buscarTodos() {
        return TipoAutorMensagem.values();
    }

    @GET
    @Path("/{id}")
    public TipoAutorMensagem buscarPorId(@PathParam("id") Long id) {
        TipoAutorMensagem v = TipoAutorMensagem.valueOf(id);
        if (v == null) throw new ResourceNotFoundException("Valor não encontrado.");
        return v;
    }
}

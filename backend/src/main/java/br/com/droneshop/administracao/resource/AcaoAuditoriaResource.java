package br.com.droneshop.administracao.resource;

import br.com.droneshop.administracao.model.AcaoAuditoria;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Rota de leitura do enum (D31). */
@Path("acoes-auditoria")
@Produces(MediaType.APPLICATION_JSON)
public class AcaoAuditoriaResource {

    @GET
    public AcaoAuditoria[] buscarTodos() {
        return AcaoAuditoria.values();
    }

    @GET
    @Path("/{id}")
    public AcaoAuditoria buscarPorId(@PathParam("id") Long id) {
        AcaoAuditoria v = AcaoAuditoria.valueOf(id);
        if (v == null) throw new ResourceNotFoundException("Valor não encontrado.");
        return v;
    }
}

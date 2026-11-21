package br.com.droneshop.identidade.resource;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.identidade.dto.SessaoResponseDTO;
import br.com.droneshop.identidade.service.AutenticacaoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Sessões ativas do cliente logado (RF50).
@Path("conta/sessoes")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class SessaoResource {

    @Inject
    JsonWebToken token;

    @Inject
    AutenticacaoService autenticacao;

    @GET
    public List<SessaoResponseDTO> listar() {
        return autenticacao.sessoesDo(Long.valueOf(token.getSubject()), SessaoAtual.id(token));
    }

    @DELETE
    @Path("{id}")
    public Response revogar(@PathParam("id") Long id) {
        autenticacao.revogar(Long.valueOf(token.getSubject()), id);
        return Response.noContent().build();
    }
}

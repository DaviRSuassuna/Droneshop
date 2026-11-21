package br.com.droneshop.identidade.resource;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.identidade.dto.ConsentimentoRequestDTO;
import br.com.droneshop.identidade.dto.ConsentimentoResponseDTO;
import br.com.droneshop.identidade.service.LgpdService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Consentimentos LGPD do cliente logado (#46). */
@Path("conta/consentimentos")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class ConsentimentoResource {

    @Inject
    JsonWebToken token;

    @Inject
    LgpdService lgpd;

    @GET
    public List<ConsentimentoResponseDTO> listar() {
        return lgpd.doUsuario(Long.valueOf(token.getSubject())).stream().map(ConsentimentoResponseDTO::de).toList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response consentir(@Valid ConsentimentoRequestDTO dto) {
        lgpd.consentir(Long.valueOf(token.getSubject()), dto);
        return Response.noContent().build();
    }
}

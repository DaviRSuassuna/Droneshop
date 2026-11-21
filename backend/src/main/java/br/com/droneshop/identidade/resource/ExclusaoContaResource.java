package br.com.droneshop.identidade.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.identidade.dto.ExclusaoContaRequestDTO;
import br.com.droneshop.identidade.service.LgpdService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Exclusão da conta com anonimização; pedidos ficam íntegros (#46). */
@Path("conta/exclusao")
@RolesAllowed("CLIENTE")
public class ExclusaoContaResource {

    @Inject
    JsonWebToken token;

    @Inject
    LgpdService lgpd;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response excluir(ExclusaoContaRequestDTO dto) {
        lgpd.excluir(Long.valueOf(token.getSubject()), dto == null ? null : dto.senha());
        return Response.noContent().build();
    }
}

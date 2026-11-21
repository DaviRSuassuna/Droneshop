package br.com.droneshop.identidade.resource;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.identidade.dto.ContaSocialResponseDTO;
import br.com.droneshop.identidade.service.GoogleService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Contas sociais vinculadas ao cliente logado (#47). */
@Path("conta/contas-sociais")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class ContaSocialResource {

    @Inject
    JsonWebToken token;

    @Inject
    GoogleService google;

    @GET
    public List<ContaSocialResponseDTO> listar() {
        return google.doUsuario(Long.valueOf(token.getSubject())).stream().map(ContaSocialResponseDTO::de).toList();
    }
}

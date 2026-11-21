package br.com.droneshop.administracao.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.administracao.dto.CodigoDoisFatoresRequestDTO;
import br.com.droneshop.administracao.dto.DoisFatoresResponseDTO;
import br.com.droneshop.administracao.service.DoisFatoresService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** 2FA do administrador logado (#45). */
@Path("conta/2fa")
@RolesAllowed("ADMINISTRATIVO")
@Produces(MediaType.APPLICATION_JSON)
public class DoisFatoresResource {

    @Inject
    JsonWebToken token;

    @Inject
    DoisFatoresService service;

    private Long usuario() {
        return Long.valueOf(token.getSubject());
    }

    @POST
    public DoisFatoresResponseDTO gerar() {
        return service.gerar(usuario());
    }

    @POST
    @Path("confirmar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmar(CodigoDoisFatoresRequestDTO dto) {
        service.confirmar(usuario(), dto == null ? null : dto.codigo());
        return Response.noContent().build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response desligar(CodigoDoisFatoresRequestDTO dto) {
        service.desligar(usuario(), dto == null ? null : dto.codigo());
        return Response.noContent().build();
    }
}

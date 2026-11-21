package br.com.droneshop.atendimento.resource;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.atendimento.dto.ChamadoDTOs.ChamadoResponseDTO;
import br.com.droneshop.atendimento.service.ChamadoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Chamados da conta do cliente, mais recente primeiro (#38). */
@Path("conta/chamados")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("CLIENTE")
public class ContaChamadoResource {

    @Inject
    ChamadoService service;

    @Inject
    JsonWebToken token;

    @GET
    public List<ChamadoResponseDTO> meus() {
        return service.doCliente(Long.valueOf(token.getSubject()));
    }
}

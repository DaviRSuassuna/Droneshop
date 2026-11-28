package br.com.droneshop.atendimento.resource;

import java.util.List;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.atendimento.dto.ChamadoDTOs.ChamadoRequestDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.ChamadoResponseDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.MensagemRequestDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.MensagemResponseDTO;
import br.com.droneshop.atendimento.service.ChamadoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Chamados de suporte do cliente logado (#38). */
@Path("chamados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("CLIENTE")
public class ChamadoResource {

    @Inject
    ChamadoService service;

    @Inject
    UsuarioAtual usuarioAtual;

    @POST
    public ChamadoResponseDTO abrir(@Valid ChamadoRequestDTO dto) {
        return service.abrir(usuarioAtual.id(), dto);
    }

    @GET
    @Path("{id}/mensagens")
    public List<MensagemResponseDTO> mensagens(@PathParam("id") Long id) {
        return service.mensagensDoCliente(usuarioAtual.id(), id);
    }

    @POST
    @Path("{id}/mensagens")
    public MensagemResponseDTO responder(@PathParam("id") Long id, @Valid MensagemRequestDTO dto) {
        return service.responderComoCliente(usuarioAtual.id(), id, dto.texto());
    }
}

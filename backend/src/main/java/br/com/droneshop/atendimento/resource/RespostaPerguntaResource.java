package br.com.droneshop.atendimento.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.atendimento.dto.PerguntaDTOs.RespostaRequestDTO;
import br.com.droneshop.atendimento.dto.PerguntaDTOs.RespostaResponseDTO;
import br.com.droneshop.atendimento.service.PerguntaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Resposta de cliente a uma pergunta aprovada (#40). */
@Path("loja/perguntas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RespostaPerguntaResource {

    @Inject
    PerguntaService service;

    @Inject
    JsonWebToken token;

    @POST
    @Path("{id}/respostas")
    @RolesAllowed("CLIENTE")
    public RespostaResponseDTO responder(@PathParam("id") Long id, @Valid RespostaRequestDTO dto) {
        return service.responder(Long.valueOf(token.getSubject()), id, dto.texto(), false);
    }
}

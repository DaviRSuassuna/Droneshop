package br.com.droneshop.atendimento.resource;

import java.util.List;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.atendimento.dto.PerguntaDTOs.PerguntaRequestDTO;
import br.com.droneshop.atendimento.dto.PerguntaDTOs.PerguntaResponseDTO;
import br.com.droneshop.atendimento.service.PerguntaService;
import jakarta.annotation.security.PermitAll;
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

/** Perguntas e respostas na página do produto (#40). */
@Path("loja/produtos/{slug}/perguntas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PerguntaResource {

    @Inject
    PerguntaService service;

    @Inject
    UsuarioAtual usuarioAtual;

    @GET
    @PermitAll
    public List<PerguntaResponseDTO> listar(@PathParam("slug") String slug) {
        return service.aprovadas(slug);
    }

    @POST
    @RolesAllowed("CLIENTE")
    public PerguntaResponseDTO perguntar(@PathParam("slug") String slug, @Valid PerguntaRequestDTO dto) {
        return service.perguntar(usuarioAtual.id(), slug, dto.texto());
    }
}

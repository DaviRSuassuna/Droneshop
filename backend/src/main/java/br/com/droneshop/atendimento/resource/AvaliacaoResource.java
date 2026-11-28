package br.com.droneshop.atendimento.resource;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacaoRequestDTO;
import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacaoResponseDTO;
import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacoesProdutoDTO;
import br.com.droneshop.atendimento.service.AvaliacaoService;
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

/** Avaliações na página do produto (#39). */
@Path("loja/produtos/{slug}/avaliacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AvaliacaoResource {

    @Inject
    AvaliacaoService service;

    @Inject
    UsuarioAtual usuarioAtual;

    @GET
    @PermitAll
    public AvaliacoesProdutoDTO listar(@PathParam("slug") String slug) {
        return service.aprovadas(slug);
    }

    @POST
    @RolesAllowed("CLIENTE")
    public AvaliacaoResponseDTO avaliar(@PathParam("slug") String slug, @Valid AvaliacaoRequestDTO dto) {
        return service.avaliar(usuarioAtual.id(), slug, dto);
    }
}

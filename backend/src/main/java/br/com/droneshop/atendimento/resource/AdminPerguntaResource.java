package br.com.droneshop.atendimento.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.atendimento.dto.PerguntaDTOs.ModeracaoRequestDTO;
import br.com.droneshop.atendimento.dto.PerguntaDTOs.PerguntaResponseDTO;
import br.com.droneshop.atendimento.dto.PerguntaDTOs.RespostaRequestDTO;
import br.com.droneshop.atendimento.dto.PerguntaDTOs.RespostaResponseDTO;
import br.com.droneshop.atendimento.service.PerguntaService;
import br.com.droneshop.dto.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Moderação e resposta oficial das perguntas (#40, RN05). */
@Path("admin/perguntas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ATENDIMENTO)
public class AdminPerguntaResource {

    @Inject
    PerguntaService service;

    @Inject
    JsonWebToken token;

    @GET
    public PageResponse<PerguntaResponseDTO> listar(@QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return service.listar(idStatus, page, pageSize);
    }

    @PATCH
    @Path("{id}")
    public void moderar(@PathParam("id") Long id, @Valid ModeracaoRequestDTO dto) {
        service.moderar(id, dto.idStatus());
    }

    @POST
    @Path("{id}/respostas")
    public RespostaResponseDTO responder(@PathParam("id") Long id, @Valid RespostaRequestDTO dto) {
        return service.responder(Long.valueOf(token.getSubject()), id, dto.texto(), true);
    }
}

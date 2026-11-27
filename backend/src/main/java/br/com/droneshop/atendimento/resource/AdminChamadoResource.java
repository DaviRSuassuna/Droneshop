package br.com.droneshop.atendimento.resource;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.ChamadoResponseDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.MensagemRequestDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.MensagemResponseDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.StatusChamadoRequestDTO;
import br.com.droneshop.atendimento.service.ChamadoService;
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

/** Atendimento dos chamados no painel (#38). */
@Path("admin/chamados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ATENDIMENTO)
public class AdminChamadoResource {

    @Inject
    ChamadoService service;

    @Inject
    UsuarioAtual usuarioAtual;

    @GET
    public PageResponse<ChamadoResponseDTO> listar(@QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return service.listar(idStatus, page, pageSize);
    }

    @GET
    @Path("{id}")
    public ChamadoResponseDTO buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    @Path("{id}/mensagens")
    public MensagemResponseDTO atender(@PathParam("id") Long id, @Valid MensagemRequestDTO dto) {
        return service.responderComoAtendente(usuarioAtual.id(), id, dto.texto());
    }

    @PATCH
    @Path("{id}/status")
    public void alterarStatus(@PathParam("id") Long id, @Valid StatusChamadoRequestDTO dto) {
        service.alterarStatus(id, dto.idStatus());
    }
}

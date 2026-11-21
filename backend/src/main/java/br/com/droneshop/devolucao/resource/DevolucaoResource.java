package br.com.droneshop.devolucao.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.devolucao.dto.DevolucaoResponseDTO;
import br.com.droneshop.devolucao.dto.StatusDevolucaoRequestDTO;
import br.com.droneshop.devolucao.model.StatusDevolucao;
import br.com.droneshop.devolucao.repository.DevolucaoRepository;
import br.com.droneshop.devolucao.service.DevolucaoService;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ValidationException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Análise das devoluções no painel (#37). */
@Path("admin/devolucoes")
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.DEVOLUCAO)
@Produces(MediaType.APPLICATION_JSON)
public class DevolucaoResource {

    @Inject
    JsonWebToken token;

    @Inject
    DevolucaoService service;

    @Inject
    DevolucaoRepository devolucoes;

    @GET
    @Transactional
    public PageResponse<DevolucaoResponseDTO> buscar(@QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        StatusDevolucao status = StatusDevolucao.valueOf(idStatus);
        if (idStatus != null && status == null)
            throw ValidationException.of("idStatus", "Status de devolução inexistente (back)");
        var q = devolucoes.filtrar(status);
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(), DevolucaoResponseDTO::de);
    }

    @GET
    @Path("{id}")
    @Transactional
    public DevolucaoResponseDTO detalhe(@PathParam("id") Long id) {
        return DevolucaoResponseDTO.de(service.buscar(id));
    }

    @PATCH
    @Path("{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response status(@PathParam("id") Long id, @Valid StatusDevolucaoRequestDTO dto) {
        service.mudarStatus(id, dto.idStatus(), dto.observacao(), Long.valueOf(token.getSubject()));
        return Response.noContent().build();
    }
}

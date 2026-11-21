package br.com.droneshop.atendimento.resource;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.atendimento.dto.LeadEmpresaDTOs.LeadEmpresaResponseDTO;
import br.com.droneshop.atendimento.dto.LeadEmpresaDTOs.StatusLeadRequestDTO;
import br.com.droneshop.atendimento.service.LeadEmpresaService;
import br.com.droneshop.dto.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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

/** Propostas comerciais recebidas (D36). */
@Path("admin/leads-empresas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ATENDIMENTO)
public class AdminLeadEmpresaResource {

    @Inject
    LeadEmpresaService service;

    @GET
    public PageResponse<LeadEmpresaResponseDTO> listar(@QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return service.listar(idStatus, page, pageSize);
    }

    @PATCH
    @Path("{id}/status")
    public void alterarStatus(@PathParam("id") Long id, @Valid StatusLeadRequestDTO dto) {
        service.alterarStatus(id, dto.idStatus());
    }
}

package br.com.droneshop.estoque.resource;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.estoque.dto.UnidadeIndividualRequestDTO;
import br.com.droneshop.estoque.dto.UnidadeIndividualResponseDTO;
import br.com.droneshop.estoque.model.StatusUnidade;
import br.com.droneshop.estoque.repository.UnidadeIndividualRepository;
import br.com.droneshop.estoque.service.UnidadeIndividualService;
import br.com.droneshop.exception.ValidationException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Unidades de drone por número de série (#34). */
@Path("unidades")
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ESTOQUE)
@Produces(MediaType.APPLICATION_JSON)
public class UnidadeIndividualResource {

    @Inject
    UnidadeIndividualService service;

    @Inject
    UnidadeIndividualRepository unidades;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public UnidadeIndividualResponseDTO criar(@Valid UnidadeIndividualRequestDTO dto) {
        return UnidadeIndividualResponseDTO.de(service.criar(dto));
    }

    @GET
    @Transactional
    public PageResponse<UnidadeIndividualResponseDTO> buscar(@QueryParam("serie") String serie,
            @QueryParam("idProduto") Long idProduto, @QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        StatusUnidade status = StatusUnidade.valueOf(idStatus);
        if (idStatus != null && status == null)
            throw ValidationException.of("idStatus", "Status de unidade inexistente (back)");
        var q = unidades.filtrar(serie, idProduto, status);
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(),
                UnidadeIndividualResponseDTO::de);
    }
}

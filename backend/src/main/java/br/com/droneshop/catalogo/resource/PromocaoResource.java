package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.catalogo.dto.PromocaoRequestDTO;
import br.com.droneshop.catalogo.dto.PromocaoResponseDTO;
import br.com.droneshop.catalogo.mapper.PromocaoResponseMapper;
import br.com.droneshop.catalogo.model.Promocao;
import br.com.droneshop.catalogo.service.PromocaoService;
import br.com.droneshop.dto.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.transaction.Transactional;

@Path("promocoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.CATALOGO)
@Transactional
public class PromocaoResource {

    @Inject
    PromocaoService service;

    @GET
    public PageResponse<PromocaoResponseDTO> buscarTodos(@QueryParam("situacao") String situacao,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Promocao> promocoes = service.findAll(situacao, page, pageSize);
        return PageResponse.of(promocoes, page, pageSize, service.count(situacao), PromocaoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public PromocaoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return PromocaoResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public PromocaoResponseDTO incluir(@Valid PromocaoRequestDTO dto) {
        return PromocaoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, @Valid PromocaoRequestDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}

package br.com.droneshop.compra.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.compra.dto.CupomRequestDTO;
import br.com.droneshop.compra.dto.CupomResponseDTO;
import br.com.droneshop.compra.dto.UsoCupomResponseDTO;
import br.com.droneshop.compra.mapper.CupomResponseMapper;
import br.com.droneshop.compra.model.Cupom;
import br.com.droneshop.compra.model.UsoCupom;
import br.com.droneshop.compra.service.CupomService;
import br.com.droneshop.dto.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

@Path("cupons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.PEDIDO)
@Transactional
public class CupomResource {

    @Inject CupomService service;

    @GET
    public PageResponse<CupomResponseDTO> buscarTodos(@QueryParam("situacao") String situacao,
            @QueryParam("codigo") String codigo, @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Cupom> cupons = service.findAll(situacao, codigo, page, pageSize);
        return PageResponse.of(cupons, page, pageSize, service.count(situacao, codigo),
                cupom -> CupomResponseMapper.toResponse(cupom, service.countUsos(cupom.getId())));
    }

    @GET
    @Path("/{id}")
    public CupomResponseDTO buscarPorId(@PathParam("id") Long id) {
        Cupom cupom = service.findById(id);
        return CupomResponseMapper.toResponse(cupom, service.countUsos(id));
    }

    @POST
    public CupomResponseDTO incluir(@Valid CupomRequestDTO dto) {
        Cupom cupom = service.create(dto);
        return CupomResponseMapper.toResponse(cupom, 0);
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, @Valid CupomRequestDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

    @GET
    @Path("/{id}/usos")
    public PageResponse<UsoCupomResponseDTO> usos(@PathParam("id") Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<UsoCupom> usos = service.usos(id, page, pageSize);
        return PageResponse.of(usos, page, pageSize, service.countUsos(id), CupomResponseMapper::toUsoResponse);
    }
}

package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.annotation.security.RolesAllowed;

import br.com.droneshop.catalogo.dto.SubcategoriaRequestDTO;
import br.com.droneshop.catalogo.dto.SubcategoriaResponseDTO;
import br.com.droneshop.catalogo.mapper.SubcategoriaResponseMapper;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.service.SubcategoriaService;

@Path("subcategorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
public class SubcategoriaResource {

    @Inject
    SubcategoriaService service;

    // ?idCategoria=2 lista so as subcategorias daquela categoria
    @GET
    public PageResponse<SubcategoriaResponseDTO> buscarTodos(@QueryParam("idCategoria") Long idCategoria,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Subcategoria> subcategorias = service.findAll(idCategoria, page, pageSize);
        long totalItems = service.count(idCategoria);

        return PageResponse.of(subcategorias, page, pageSize, totalItems, SubcategoriaResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<SubcategoriaResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Subcategoria> subcategorias = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(subcategorias, page, pageSize, totalItems, SubcategoriaResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public SubcategoriaResponseDTO buscarPorId(@PathParam("id") Long id) {
        return SubcategoriaResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public SubcategoriaResponseDTO incluir(SubcategoriaRequestDTO dto) {
        return SubcategoriaResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, SubcategoriaRequestDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

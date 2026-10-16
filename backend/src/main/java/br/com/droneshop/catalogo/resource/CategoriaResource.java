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
import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;

import br.com.droneshop.catalogo.dto.CategoriaRequestDTO;
import br.com.droneshop.catalogo.dto.CategoriaResponseDTO;
import br.com.droneshop.catalogo.mapper.CategoriaResponseMapper;
import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.service.CategoriaService;

@Path("categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.CATALOGO)
public class CategoriaResource {

    @Inject
    CategoriaService service;

    @GET
    public PageResponse<CategoriaResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Categoria> categorias = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(categorias, page, pageSize, totalItems, CategoriaResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<CategoriaResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Categoria> categorias = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(categorias, page, pageSize, totalItems, CategoriaResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public CategoriaResponseDTO buscarPorId(@PathParam("id") Long id) {
        return CategoriaResponseMapper.toResponse(service.findById(id));
    }

    @GET
    @Path("/slug/{slug}")
    public CategoriaResponseDTO buscarPorSlug(@PathParam("slug") String slug) {
        return CategoriaResponseMapper.toResponse(service.findBySlug(slug));
    }

    @POST
    public CategoriaResponseDTO incluir(CategoriaRequestDTO dto) {
        return CategoriaResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, CategoriaRequestDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

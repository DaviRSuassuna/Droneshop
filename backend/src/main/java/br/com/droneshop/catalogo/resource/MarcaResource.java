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

import br.com.droneshop.catalogo.dto.MarcaRequestDTO;
import br.com.droneshop.catalogo.dto.MarcaResponseDTO;
import br.com.droneshop.catalogo.mapper.MarcaResponseMapper;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.service.MarcaService;

@Path("marcas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MarcaResource {

    @Inject
    MarcaService service;

    @GET
    public PageResponse<MarcaResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Marca> marcas = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(marcas, page, pageSize, totalItems, MarcaResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<MarcaResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Marca> marcas = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(marcas, page, pageSize, totalItems, MarcaResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public MarcaResponseDTO buscarPorId(@PathParam("id") Long id) {
        return MarcaResponseMapper.toResponse(service.findById(id));
    }

    @GET
    @Path("/slug/{slug}")
    public MarcaResponseDTO buscarPorSlug(@PathParam("slug") String slug) {
        return MarcaResponseMapper.toResponse(service.findBySlug(slug));
    }

    @POST
    public MarcaResponseDTO incluir(MarcaRequestDTO dto) {
        return MarcaResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, MarcaRequestDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

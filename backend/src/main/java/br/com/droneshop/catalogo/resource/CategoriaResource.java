package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.dto.CategoriaDTO;
import br.com.droneshop.catalogo.dto.CategoriaResponseDTO;
import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.service.CategoriaService;
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

@Path("categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    @Inject
    CategoriaService service;

    // Categoria e serializada via CategoriaResponseDTO, no mesmo padrao de MarcaResource.

    @GET
    public List<CategoriaResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findAll(page, pageSize).stream().map(CategoriaResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/nome/{nome}")
    public List<CategoriaResponseDTO> buscarPorNome(@PathParam("nome") String nome, @QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findByNome(nome, page, pageSize).stream().map(CategoriaResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/{id}")
    public CategoriaResponseDTO buscarPorId(@PathParam("id") Long id) {
        Categoria categoria = service.findById(id);
        return (categoria == null) ? null : CategoriaResponseDTO.valueOf(categoria);
    }

    @GET
    @Path("/slug/{slug}")
    public CategoriaResponseDTO buscarPorSlug(@PathParam("slug") String slug) {
        Categoria categoria = service.findBySlug(slug);
        return (categoria == null) ? null : CategoriaResponseDTO.valueOf(categoria);
    }

    @POST
    public CategoriaResponseDTO incluir(CategoriaDTO dto) {
        return CategoriaResponseDTO.valueOf(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, CategoriaDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

    @GET
    @Path("/count")
    public Long total() {
        return service.count();
    }

    @GET
    @Path("/nome/{nome}/count")
    public Long totalPorNome(@PathParam("nome") String nome) {
        return service.count(nome);
    }

}

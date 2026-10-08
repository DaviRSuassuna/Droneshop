package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.dto.SubcategoriaDTO;
import br.com.droneshop.catalogo.dto.SubcategoriaResponseDTO;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.service.SubcategoriaService;
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

@Path("subcategorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubcategoriaResource {

    @Inject
    SubcategoriaService service;

    // GET /subcategorias?categoriaId=2 lista so as subcategorias daquela categoria
    @GET
    public List<SubcategoriaResponseDTO> buscarTodos(@QueryParam("categoriaId") Long categoriaId,
                                                     @QueryParam("page") @DefaultValue("0") int page,
                                                     @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findAll(categoriaId, page, pageSize).stream().map(SubcategoriaResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/{id}")
    public SubcategoriaResponseDTO buscarPorId(@PathParam("id") Long id) {
        Subcategoria subcategoria = service.findById(id);
        return (subcategoria == null) ? null : SubcategoriaResponseDTO.valueOf(subcategoria);
    }

    @POST
    public SubcategoriaResponseDTO incluir(SubcategoriaDTO dto) {
        return SubcategoriaResponseDTO.valueOf(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, SubcategoriaDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

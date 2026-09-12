package br.com.droneshop.catalogo.resource;

import java.util.List;
import java.util.stream.Collectors;
import br.com.droneshop.catalogo.dto.CategoriaDTO;
import br.com.droneshop.catalogo.dto.CategoriaResponseDTO;
import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.service.CategoriaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {
    @Inject CategoriaService service;

    @GET public List<CategoriaResponseDTO> listar(@QueryParam("page") @DefaultValue("0") int page,
                                                   @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findAll(page, pageSize).stream().map(CategoriaResponseDTO::valueOf).collect(Collectors.toList());
    }
    @GET @Path("/{id}") public CategoriaResponseDTO buscar(@PathParam("id") long id) {
        Categoria categoria = service.findById(id);
        return categoria == null ? null : CategoriaResponseDTO.valueOf(categoria);
    }
    @POST public CategoriaResponseDTO criar(CategoriaDTO dto) { return CategoriaResponseDTO.valueOf(service.create(dto)); }
    @PUT @Path("/{id}") public void atualizar(@PathParam("id") long id, CategoriaDTO dto) { service.update(id, dto); }
    @DELETE @Path("/{id}") public void excluir(@PathParam("id") long id) { service.delete(id); }
}

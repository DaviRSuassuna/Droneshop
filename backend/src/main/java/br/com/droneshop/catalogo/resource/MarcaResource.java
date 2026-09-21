package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.dto.MarcaDTO;
import br.com.droneshop.catalogo.dto.MarcaResponseDTO;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.service.MarcaService;
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

@Path("marcas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MarcaResource {

    @Inject
    MarcaService service;

    @GET
    public List<MarcaResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findAll(page, pageSize).stream().map(MarcaResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/nome/{nome}")
    public List<MarcaResponseDTO> buscarPorNome(@PathParam("nome") String nome, @QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findByNome(nome, page, pageSize).stream().map(MarcaResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/{id}")
    public MarcaResponseDTO buscarPorId(@PathParam("id") Long id) {
        Marca marca = service.findById(id);
        return (marca == null) ? null : MarcaResponseDTO.valueOf(marca);
    }

    @GET
    @Path("/slug/{slug}")
    public MarcaResponseDTO buscarPorSlug(@PathParam("slug") String slug) {
        Marca marca = service.findBySlug(slug);
        return (marca == null) ? null : MarcaResponseDTO.valueOf(marca);
    }

    @POST
    public MarcaResponseDTO incluir(MarcaDTO dto) {
        return MarcaResponseDTO.valueOf(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, MarcaDTO dto) {
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

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

import br.com.droneshop.catalogo.dto.ProdutoRequestDTO;
import br.com.droneshop.catalogo.dto.ProdutoResponseDTO;
import br.com.droneshop.catalogo.mapper.ProdutoResponseMapper;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.service.ProdutoService;

@Path("produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    @Inject
    ProdutoService service;

    // filtros combinaveis; cada um vazio e ignorado. nome busca tambem no modelo
    @GET
    public PageResponse<ProdutoResponseDTO> buscarTodos(@QueryParam("nome") String nome,
            @QueryParam("idMarca") Long idMarca,
            @QueryParam("idCategoria") Long idCategoria,
            @QueryParam("idSubcategoria") Long idSubcategoria,
            @QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Produto> produtos = service.findComFiltro(nome, idMarca, idCategoria, idSubcategoria, idStatus, page, pageSize);
        long totalItems = service.countComFiltro(nome, idMarca, idCategoria, idSubcategoria, idStatus);

        return PageResponse.of(produtos, page, pageSize, totalItems, ProdutoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public ProdutoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return ProdutoResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public ProdutoResponseDTO incluir(ProdutoRequestDTO dto) {
        return ProdutoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, ProdutoRequestDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

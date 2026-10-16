package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.dto.ProdutoResponseDTO;
import br.com.droneshop.catalogo.mapper.ProdutoResponseMapper;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.service.ProdutoService;
import br.com.droneshop.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.annotation.security.RolesAllowed;
import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;

// Lista, consulta e exclui todos os tipos. Cadastro e alteracao ficam em /drones e
// /acessorios (D27), entao POST e PUT aqui respondem 405.
@Path("produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.CATALOGO)
public class ProdutoResource {

    @Inject
    ProdutoService service;

    // filtros combinaveis; cada um vazio e ignorado. nome busca tambem no modelo
    @GET
    public PageResponse<ProdutoResponseDTO> buscarTodos(@QueryParam("idTipo") Long idTipo,
            @QueryParam("nome") String nome,
            @QueryParam("idMarca") Long idMarca,
            @QueryParam("idCategoria") Long idCategoria,
            @QueryParam("idSubcategoria") Long idSubcategoria,
            @QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Produto> produtos = service.findComFiltro(idTipo, nome, idMarca, idCategoria, idSubcategoria, idStatus,
                page, pageSize);
        long totalItems = service.countComFiltro(idTipo, nome, idMarca, idCategoria, idSubcategoria, idStatus);

        return PageResponse.of(produtos, page, pageSize, totalItems, ProdutoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public ProdutoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return ProdutoResponseMapper.toResponse(service.findById(id));
    }

    @GET
    @Path("/slug/{slug}")
    public ProdutoResponseDTO buscarPorSlug(@PathParam("slug") String slug) {
        return ProdutoResponseMapper.toResponse(service.findBySlug(slug));
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

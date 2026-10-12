package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.dto.ProdutoDTO;
import br.com.droneshop.catalogo.dto.ProdutoResponseDTO;
import br.com.droneshop.catalogo.service.ProdutoService;
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

@Path("produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    @Inject
    ProdutoService service;

    // GET /produtos?nome=&marcaId=&categoriaId=&subcategoriaId=&status=&page=0&pageSize=10
    // filtro e paginacao feitos no banco; todos os filtros sao opcionais
    @GET
    public List<ProdutoResponseDTO> buscar(@QueryParam("nome") String nome,
                                           @QueryParam("marcaId") Long marcaId,
                                           @QueryParam("categoriaId") Long categoriaId,
                                           @QueryParam("subcategoriaId") Long subcategoriaId,
                                           @QueryParam("status") String status,
                                           @QueryParam("page") @DefaultValue("0") int page,
                                           @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findComFiltro(nome, marcaId, categoriaId, subcategoriaId, status, page, pageSize)
                .stream().map(ProdutoResponseDTO::valueOf).toList();
    }

    // mesmo filtro do GET acima, devolvendo so o total (para montar a paginacao)
    @GET
    @Path("/count")
    public Long total(@QueryParam("nome") String nome,
                      @QueryParam("marcaId") Long marcaId,
                      @QueryParam("categoriaId") Long categoriaId,
                      @QueryParam("subcategoriaId") Long subcategoriaId,
                      @QueryParam("status") String status) {
        return service.countComFiltro(nome, marcaId, categoriaId, subcategoriaId, status);
    }

    @GET
    @Path("/{id}")
    public ProdutoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return ProdutoResponseDTO.valueOf(service.findById(id));
    }

    @POST
    public ProdutoResponseDTO incluir(ProdutoDTO dto) {
        return ProdutoResponseDTO.valueOf(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, ProdutoDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

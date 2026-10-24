package br.com.droneshop.estoque.resource;

import java.util.Arrays;
import br.com.droneshop.util.Csv;
import jakarta.ws.rs.core.Response;
import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.estoque.dto.EstoqueResponseDTO;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
import br.com.droneshop.estoque.mapper.EstoqueResponseMapper;
import br.com.droneshop.estoque.service.MovimentoEstoqueService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Saldo por produto, kits inclusive (disponivel derivado dos componentes)
@Path("estoque")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ESTOQUE)
public class EstoqueResource {

    @Inject
    MovimentoEstoqueService service;

    @GET
    @Path("/exportar")
    @Produces("text/csv")
    public Response exportar(@QueryParam("situacao") String situacaoParam,
            @QueryParam("busca") String busca, @QueryParam("idMarca") Long idMarca) {
        var pagina = buscarTodos(situacaoParam, busca, idMarca, 0, Integer.MAX_VALUE);
        return Csv.resposta("estoque.csv", List.of("Produto", "SKU", "Físico", "Reservado", "Disponível", "Mínimo"),
                pagina.items().stream().map(p -> Arrays.<Object>asList(p.produto().nome(), p.produto().sku(),
                        p.estoqueFisico(), p.reservado(), p.disponivel(), p.estoqueMinimo())));
    }

    // busca no SKU e no nome do produto; idMarca filtra pela marca
    @GET
    public PageResponse<EstoqueResponseDTO> buscarTodos(@QueryParam("situacao") String situacaoParam,
            @QueryParam("busca") String busca,
            @QueryParam("idMarca") Long idMarca,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        SituacaoEstoque situacao = service.situacao(situacaoParam);
        List<Produto> produtos = service.findEstoque(situacao, busca, idMarca, page, pageSize);
        long totalItems = service.countEstoque(situacao, busca, idMarca);

        return PageResponse.of(produtos, page, pageSize, totalItems, EstoqueResponseMapper::toEstoque);
    }

}

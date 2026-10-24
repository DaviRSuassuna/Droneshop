package br.com.droneshop.estoque.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.estoque.dto.MovimentoEstoqueRequestDTO;
import br.com.droneshop.estoque.dto.MovimentoEstoqueResponseDTO;
import br.com.droneshop.estoque.mapper.EstoqueResponseMapper;
import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.estoque.model.TipoMovimento;
import br.com.droneshop.estoque.service.MovimentoEstoqueService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Sem PUT/DELETE (405): movimento nao se edita nem se exclui, corrige-se com outro
@Path("estoque/movimentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ESTOQUE)
public class MovimentoEstoqueResource {

    @Inject
    MovimentoEstoqueService service;

    @GET
    public PageResponse<MovimentoEstoqueResponseDTO> buscarTodos(@QueryParam("idProduto") Long idProduto,
            @QueryParam("idTipo") String idTipo,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        TipoMovimento tipo = service.tipo(idTipo);
        List<MovimentoEstoque> movimentos = service.findComFiltro(idProduto, tipo, page, pageSize);
        long totalItems = service.countComFiltro(idProduto, tipo);

        return PageResponse.of(movimentos, page, pageSize, totalItems, EstoqueResponseMapper::toResponse);
    }

    @POST
    public MovimentoEstoqueResponseDTO incluir(MovimentoEstoqueRequestDTO dto) {
        return EstoqueResponseMapper.toResponse(service.registrar(dto));
    }

}

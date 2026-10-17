package br.com.droneshop.estoque.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.catalogo.model.VarianteProduto;
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

// Saldo por variante, kits inclusive (disponivel derivado dos componentes)
@Path("estoque")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ESTOQUE)
public class EstoqueResource {

    @Inject
    MovimentoEstoqueService service;

    // busca no SKU, no nome da variante e no do produto
    @GET
    public PageResponse<EstoqueResponseDTO> buscarTodos(@QueryParam("situacao") String situacaoParam,
            @QueryParam("busca") String busca,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        SituacaoEstoque situacao = service.situacao(situacaoParam);
        List<VarianteProduto> variantes = service.findEstoque(situacao, busca, page, pageSize);
        long totalItems = service.countEstoque(situacao, busca);

        return PageResponse.of(variantes, page, pageSize, totalItems, EstoqueResponseMapper::toEstoque);
    }

}

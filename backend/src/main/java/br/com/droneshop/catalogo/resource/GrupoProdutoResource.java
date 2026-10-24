package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.catalogo.dto.GrupoProdutoRequestDTO;
import br.com.droneshop.catalogo.dto.GrupoProdutoResponseDTO;
import br.com.droneshop.catalogo.mapper.GrupoProdutoResponseMapper;
import br.com.droneshop.catalogo.model.GrupoProduto;
import br.com.droneshop.catalogo.service.GrupoProdutoService;
import br.com.droneshop.dto.PageResponse;
import jakarta.annotation.security.RolesAllowed;
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

// Grupos de produto (D40). O vinculo produto -> grupo e gravado pelo cadastro do produto
// (idGrupo, rotuloNoGrupo, ordemNoGrupo).
@Path("grupos-produto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.CATALOGO)
public class GrupoProdutoResource {

    @Inject
    GrupoProdutoService service;

    // "ativo=" vazio e ignorado (Boolean leria como false)
    @GET
    public PageResponse<GrupoProdutoResponseDTO> buscarTodos(@QueryParam("nome") String nome,
            @QueryParam("ativo") String ativoParam,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        Boolean ativo = (ativoParam == null || ativoParam.isBlank()) ? null : Boolean.valueOf(ativoParam.trim());
        List<GrupoProduto> grupos = service.findComFiltro(nome, ativo, page, pageSize);
        long totalItems = service.count(nome, ativo);

        return PageResponse.of(grupos, page, pageSize, totalItems, GrupoProdutoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public GrupoProdutoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return GrupoProdutoResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public GrupoProdutoResponseDTO incluir(GrupoProdutoRequestDTO dto) {
        return GrupoProdutoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, GrupoProdutoRequestDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

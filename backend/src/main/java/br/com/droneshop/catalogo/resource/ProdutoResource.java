package br.com.droneshop.catalogo.resource;

import java.util.Arrays;
import jakarta.ws.rs.PATCH;
import jakarta.validation.Valid;
import br.com.droneshop.util.Csv;
import jakarta.ws.rs.core.Response;
import br.com.droneshop.catalogo.service.KitCalculo;
import br.com.droneshop.catalogo.dto.ProdutoLoteRequestDTO;
import br.com.droneshop.catalogo.service.ProdutoLoteService;
import br.com.droneshop.catalogo.dto.ProdutoLoteResponseDTO;
import java.util.List;

import br.com.droneshop.catalogo.dto.ProdutoResponseDTO;
import br.com.droneshop.catalogo.mapper.ProdutoResponseMapper;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.service.ProdutoService;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
import br.com.droneshop.estoque.service.MovimentoEstoqueService;
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

// Lista, consulta e exclui todos os tipos. Cadastro e alteracao ficam em /drones,
// /acessorios e /kits (D27), entao POST e PUT aqui respondem 405.
@Path("produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.CATALOGO)
public class ProdutoResource {

    @Inject
    ProdutoService service;

    @Inject
    ProdutoLoteService loteService;

    @PATCH
    @Path("/lote")
    public ProdutoLoteResponseDTO alterarEmLote(
            @Valid ProdutoLoteRequestDTO dto) {
        return loteService.executar(dto);
    }

    @Inject
    MovimentoEstoqueService estoqueService;

    // filtros combinaveis; cada um vazio e ignorado. nome (ou busca, o mesmo filtro) procura
    // no nome e no SKU. Select de produto do admin (D40): idStatus filtra pelo enum StatusProduto e
    // kit=false tira os kits (estoque e componente de kit nao aceitam kit). estoque=ABAIXO_MINIMO
    // ou ZERADO segue a regra do GET /estoque (#13).
    @GET
    @Path("/exportar")
    @Produces("text/csv")
    public Response exportar(@QueryParam("idTipo") Long idTipo,
            @QueryParam("nome") String nome,
            @QueryParam("busca") String busca,
            @QueryParam("idMarca") Long idMarca,
            @QueryParam("idCategoria") Long idCategoria,
            @QueryParam("idSubcategoria") Long idSubcategoria,
            @QueryParam("idStatus") Long idStatus,
            @QueryParam("idGrupo") Long idGrupo,
            @QueryParam("kit") String kitParam,
            @QueryParam("estoque") String estoqueParam) {
        String termo = (busca != null && !busca.isBlank()) ? busca : nome;
        var produtos = service.findComFiltro(idTipo, termo, idMarca, idCategoria, idSubcategoria, idStatus, idGrupo,
                Boolean.FALSE.equals(booleano(kitParam)),
                estoqueService.situacao(estoqueParam, "estoque"), 0, Integer.MAX_VALUE);
        return Csv.resposta("produtos.csv", List.of("Produto", "SKU", "Tipo", "Grupo", "Marca", "Preço", "Estoque", "Disponível", "Status"),
                produtos.stream().map(p -> Arrays.<Object>asList(p.getNome(), p.getSku(), p.getTipo().getNome(),
                        p.getGrupo() == null ? null : p.getGrupo().getNome(), p.getMarca() == null ? null : p.getMarca().getNome(),
                        p.getPreco(), p.getEstoqueFisico(), KitCalculo.disponivel(p), p.getStatus().getNome())));
    }

    @GET
    public PageResponse<ProdutoResponseDTO> buscarTodos(@QueryParam("idTipo") Long idTipo,
            @QueryParam("nome") String nome,
            @QueryParam("busca") String busca,
            @QueryParam("idMarca") Long idMarca,
            @QueryParam("idCategoria") Long idCategoria,
            @QueryParam("idSubcategoria") Long idSubcategoria,
            @QueryParam("idStatus") Long idStatus,
            @QueryParam("idGrupo") Long idGrupo,
            @QueryParam("kit") String kitParam,
            @QueryParam("estoque") String estoqueParam,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        String termo = (busca != null && !busca.isBlank()) ? busca : nome;
        boolean semKit = Boolean.FALSE.equals(booleano(kitParam));
        SituacaoEstoque estoque = estoqueService.situacao(estoqueParam, "estoque");
        List<Produto> produtos = service.findComFiltro(idTipo, termo, idMarca, idCategoria, idSubcategoria, idStatus,
                idGrupo, semKit, estoque, page, pageSize);
        long totalItems = service.countComFiltro(idTipo, termo, idMarca, idCategoria, idSubcategoria, idStatus,
                idGrupo, semKit, estoque);

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

    // "kit=" vazio é ignorado, sem excluir kits por engano.
    private static Boolean booleano(String valor) {
        return (valor == null || valor.isBlank()) ? null : Boolean.valueOf(valor.trim());
    }

}

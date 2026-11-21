package br.com.droneshop.catalogo.resource;

import java.math.BigDecimal;
import java.util.List;

import br.com.droneshop.catalogo.dto.CategoriaLojaDTO;
import br.com.droneshop.catalogo.dto.FiltrosLojaDTO;
import br.com.droneshop.catalogo.dto.MarcaLojaDTO;
import br.com.droneshop.catalogo.dto.OfertaLojaResponseDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaDetalheDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaItemDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaResumoDTO;
import br.com.droneshop.catalogo.service.LojaComplementoService;
import br.com.droneshop.catalogo.service.LojaService;
import br.com.droneshop.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Rotas públicas da vitrine (#21): só o que está ATIVO aparece (RN17). */
@Path("loja")
@Produces(MediaType.APPLICATION_JSON)
public class LojaResource {

    @Inject
    LojaService service;

    @Inject
    LojaComplementoService complementoService;

    @Inject
    br.com.droneshop.catalogo.service.ComparacaoService comparacao;

    @GET
    @Path("/categorias")
    public List<CategoriaLojaDTO> listarCategorias(@QueryParam("tipo") String tipo) {
        return service.listarCategorias(tipo);
    }

    @GET
    @Path("/marcas")
    public List<MarcaLojaDTO> listarMarcas(@QueryParam("destaque") Boolean destaque) {
        return service.listarMarcas(destaque);
    }

    @GET
    @Path("/produtos")
    public PageResponse<ProdutoLojaItemDTO> listarProdutos(
            @QueryParam("categoria") String categoria,
            @QueryParam("subcategoria") String subcategoria,
            @QueryParam("marca") String marca,
            @QueryParam("tipo") String tipo,
            @QueryParam("precoMin") BigDecimal precoMin,
            @QueryParam("precoMax") BigDecimal precoMax,
            @QueryParam("autonomiaMin") Integer autonomiaMin,
            @QueryParam("alcanceMin") Integer alcanceMin,
            @QueryParam("nome") String nome,
            @QueryParam("ordem") String ordem,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("12") int pageSize) {
        return service.listarProdutos(new LojaService.Filtro(categoria, subcategoria, marca, tipo, precoMin, precoMax,
                autonomiaMin, alcanceMin, nome, null), ordem, page, pageSize);
    }

    @GET
    @Path("/comparar")
    public br.com.droneshop.catalogo.dto.ComparacaoDTO comparar(@QueryParam("grupo") String grupo,
            @QueryParam("ids") String ids) {
        return comparacao.comparar(grupo, ids);
    }

    @GET
    @Path("/busca")
    public PageResponse<ProdutoLojaItemDTO> buscar(@QueryParam("q") String q,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("12") int pageSize) {
        return service.buscar(q, page, pageSize);
    }

    @GET
    @Path("/filtros")
    public FiltrosLojaDTO buscarFiltros(@QueryParam("categoria") String categoria) {
        return service.buscarFiltros(categoria);
    }

    @GET
    @Path("/produtos/{slug}")
    public ProdutoLojaDetalheDTO buscarProduto(@PathParam("slug") String slug) {
        return service.buscarProduto(slug);
    }

    @GET
    @Path("/produtos/{slug}/compativeis")
    public List<ProdutoLojaResumoDTO> buscarCompativeis(@PathParam("slug") String slug) {
        return complementoService.buscarCompativeis(slug);
    }

    @GET
    @Path("/ofertas")
    public List<OfertaLojaResponseDTO> buscarOfertas() {
        return complementoService.buscarOfertas();
    }
}

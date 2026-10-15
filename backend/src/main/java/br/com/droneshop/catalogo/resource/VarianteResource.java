package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.dto.VarianteListagemResponseDTO;
import br.com.droneshop.catalogo.dto.VarianteRequestDTO;
import br.com.droneshop.catalogo.dto.VarianteResponseDTO;
import br.com.droneshop.catalogo.mapper.VarianteResponseMapper;
import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.catalogo.service.VarianteService;
import br.com.droneshop.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Criar e excluir variante e pelo corpo de /drones e /acessorios (D13)
@Path("variantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VarianteResource {

    @Inject
    VarianteService service;

    @GET
    public PageResponse<VarianteListagemResponseDTO> buscarTodos(@QueryParam("busca") String busca,
            @QueryParam("ativa") String ativaParam,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        // "ativa=" vazio e ignorado (Boolean leria como false)
        Boolean ativa = (ativaParam == null || ativaParam.isBlank()) ? null : Boolean.valueOf(ativaParam.trim());
        List<VarianteProduto> variantes = service.findComFiltro(busca, ativa, page, pageSize);
        long totalItems = service.countComFiltro(busca, ativa);

        return PageResponse.of(variantes, page, pageSize, totalItems, VarianteResponseMapper::toListagem);
    }

    @GET
    @Path("/{id}")
    public VarianteResponseDTO buscarPorId(@PathParam("id") Long id) {
        return VarianteResponseMapper.toResponse(service.findById(id));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, VarianteRequestDTO dto) {
        service.update(id, dto);
    }

}

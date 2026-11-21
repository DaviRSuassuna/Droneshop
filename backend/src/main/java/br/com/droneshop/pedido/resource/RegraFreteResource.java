package br.com.droneshop.pedido.resource;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.pedido.dto.RegraFreteRequestDTO;
import br.com.droneshop.pedido.dto.RegraFreteResponseDTO;
import br.com.droneshop.pedido.repository.RegraFreteRepository;
import br.com.droneshop.pedido.service.FreteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
import jakarta.ws.rs.core.Response;

/** RegraFrete: retirada no showroom e fallback do agregador (#30). */
@Path("admin/regras-frete")
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.PEDIDO)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegraFreteResource {

    @Inject
    FreteService frete;

    @Inject
    RegraFreteRepository regras;

    @GET
    public PageResponse<RegraFreteResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        var q = regras.findAll(io.quarkus.panache.common.Sort.by("id"));
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(), RegraFreteResponseDTO::de);
    }

    @GET
    @Path("{id}")
    public RegraFreteResponseDTO buscar(@PathParam("id") Long id) {
        return RegraFreteResponseDTO.de(frete.buscar(id));
    }

    @POST
    public RegraFreteResponseDTO criar(@Valid RegraFreteRequestDTO dto) {
        return RegraFreteResponseDTO.de(frete.criar(dto));
    }

    @PUT
    @Path("{id}")
    public RegraFreteResponseDTO alterar(@PathParam("id") Long id, @Valid RegraFreteRequestDTO dto) {
        return RegraFreteResponseDTO.de(frete.alterar(id, dto));
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@PathParam("id") Long id) {
        frete.excluir(id);
        return Response.noContent().build();
    }
}

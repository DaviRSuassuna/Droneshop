package br.com.droneshop.pedido.resource;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.pedido.dto.HistoricoStatusPedidoResponseDTO;
import br.com.droneshop.pedido.dto.PedidoFiltro;
import br.com.droneshop.pedido.dto.PedidoListagemResponseDTO;
import br.com.droneshop.pedido.dto.PedidoResponseDTO;
import br.com.droneshop.pedido.dto.ResumoStatusPedidoResponseDTO;
import br.com.droneshop.pedido.dto.StatusPedidoRequestDTO;
import br.com.droneshop.pedido.mapper.PedidoResponseMapper;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.service.PedidoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Sem POST/PUT/DELETE (405): o pedido nasce no checkout (#26) e só muda pelo PATCH de status
@Path("pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.PEDIDO)
public class PedidoResource {

    @Inject
    PedidoService service;

    @Inject
    JsonWebToken jwt;

    // datas sobre dataCadastro, inclusivas (AAAA-MM-DD)
    @GET
    public PageResponse<PedidoListagemResponseDTO> buscarTodos(@QueryParam("idStatus") String idStatus,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim,
            @QueryParam("busca") String busca,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        PedidoFiltro filtro = service.filtro(idStatus, dataInicio, dataFim, busca);
        List<Pedido> pedidos = service.findComFiltro(filtro, page, pageSize);
        long totalItems = service.countComFiltro(filtro);

        return PageResponse.of(pedidos, page, pageSize, totalItems, PedidoResponseMapper::toListagem);
    }

    @GET
    @Path("/resumo-status")
    public List<ResumoStatusPedidoResponseDTO> resumoStatus() {
        return service.resumoPorStatus();
    }

    @GET
    @Path("/{id}")
    public PedidoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return PedidoResponseMapper.toResponse(service.findById(id));
    }

    @PATCH
    @Path("/{id}/status")
    public Response alterarStatus(@PathParam("id") Long id, @Valid StatusPedidoRequestDTO dto) {
        service.alterarStatus(id, dto, idUsuarioLogado());
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/historico")
    public List<HistoricoStatusPedidoResponseDTO> historico(@PathParam("id") Long id) {
        return service.historico(id).stream().map(PedidoResponseMapper::toHistorico).toList();
    }

    // o autor da mudança manual é o administrador do token (D11)
    private long idUsuarioLogado() {
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (RuntimeException e) {
            throw new NotAuthorizedException("Credenciais ausentes ou inválidas.");
        }
    }

}

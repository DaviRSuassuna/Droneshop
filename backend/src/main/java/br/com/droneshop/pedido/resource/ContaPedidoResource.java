package br.com.droneshop.pedido.resource;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.pedido.dto.NotaFiscalResponseDTO;
import br.com.droneshop.pedido.dto.PedidoListagemResponseDTO;
import br.com.droneshop.pedido.dto.RastreioResponseDTO;
import br.com.droneshop.pedido.service.NotaFiscalService;
import br.com.droneshop.pedido.dto.PedidoResponseDTO;
import br.com.droneshop.pedido.mapper.PedidoResponseMapper;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** "Meus pedidos": só os pedidos do cliente logado; de outro cliente é 404 (#29). */
@Path("conta/pedidos")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class ContaPedidoResource {

    @Inject
    JsonWebToken token;

    @Inject
    PedidoRepository pedidos;

    @Inject
    NotaFiscalService notasFiscais;

    private Long usuario() {
        return Long.valueOf(token.getSubject());
    }

    /** Pedido do cliente logado, ou 404. */
    public Pedido doCliente(Long id) {
        Pedido p = pedidos.doUsuario(id, usuario());
        if (p == null)
            throw new ResourceNotFoundException("Pedido não encontrado.");
        return p;
    }

    @GET
    @Transactional
    public PageResponse<PedidoListagemResponseDTO> listar(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        var q = pedidos.doUsuario(usuario());
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(),
                PedidoResponseMapper::toListagem);
    }

    @GET
    @Path("{id}")
    @Transactional
    public PedidoResponseDTO buscar(@PathParam("id") Long id) {
        return PedidoResponseMapper.toResponse(doCliente(id));
    }

    @GET
    @Path("{id}/rastreio")
    @Transactional
    public RastreioResponseDTO rastreio(@PathParam("id") Long id) {
        return RastreioResponseDTO.de(doCliente(id).getEntrega());
    }

    @GET
    @Path("{id}/nota-fiscal")
    @Transactional
    public List<NotaFiscalResponseDTO> notas(@PathParam("id") Long id) {
        return notasFiscais.doPedido(doCliente(id).getId()).stream().map(NotaFiscalResponseDTO::de).toList();
    }
}

package br.com.droneshop.pagamento.resource;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.pagamento.dto.CartaoSalvoRequestDTO;
import br.com.droneshop.pagamento.dto.CartaoSalvoResponseDTO;
import br.com.droneshop.pagamento.service.CartaoSalvoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Meios de pagamento do cliente logado (#28). */
@Path("conta/cartoes")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class CartaoSalvoResource {

    @Inject
    JsonWebToken token;

    @Inject
    CartaoSalvoService cartoes;

    private Long usuario() {
        return Long.valueOf(token.getSubject());
    }

    @GET
    public List<CartaoSalvoResponseDTO> listar() {
        return cartoes.listar(usuario()).stream().map(CartaoSalvoResponseDTO::de).toList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CartaoSalvoResponseDTO salvar(@Valid CartaoSalvoRequestDTO dto) {
        return CartaoSalvoResponseDTO.de(cartoes.salvar(usuario(), dto));
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@PathParam("id") Long id) {
        cartoes.excluir(usuario(), id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}/padrao")
    public Response tornarPadrao(@PathParam("id") Long id) {
        cartoes.tornarPadrao(usuario(), id);
        return Response.noContent().build();
    }
}

package br.com.droneshop.pedido.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.compra.resource.CarrinhoResource;
import br.com.droneshop.pedido.dto.CheckoutRequestDTO;
import br.com.droneshop.pedido.dto.PedidoResponseDTO;
import br.com.droneshop.pedido.mapper.PedidoResponseMapper;
import br.com.droneshop.pedido.service.CheckoutService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Confirma o checkout: carrinho do header vira pedido PENDENTE (#26). */
@Path("checkout")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CheckoutResource {

    @Inject
    JsonWebToken token;

    @Inject
    CheckoutService checkout;

    @POST
    public PedidoResponseDTO fechar(@HeaderParam(CarrinhoResource.HEADER) String carrinho,
            @Valid CheckoutRequestDTO dto) {
        return PedidoResponseMapper.toResponse(checkout.fechar(Long.valueOf(token.getSubject()), carrinho, dto));
    }
}

package br.com.droneshop.compra.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.compra.dto.CarrinhoResponseDTO;
import br.com.droneshop.compra.service.CarrinhoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Carrinho ATIVO do cliente logado; cria vazio se não houver (#24). */
@Path("conta/carrinho")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class ContaCarrinhoResource {

    @Inject
    CarrinhoService carrinhos;

    @Inject
    JsonWebToken token;

    @GET
    public CarrinhoResponseDTO doCliente() {
        return carrinhos.doCliente(Long.valueOf(token.getSubject()));
    }
}

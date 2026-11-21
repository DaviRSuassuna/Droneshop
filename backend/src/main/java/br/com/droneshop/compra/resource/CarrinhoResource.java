package br.com.droneshop.compra.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.compra.dto.CarrinhoResponseDTO;
import br.com.droneshop.compra.dto.CupomCarrinhoRequestDTO;
import br.com.droneshop.compra.dto.ItemCarrinhoRequestDTO;
import br.com.droneshop.compra.dto.QuantidadeRequestDTO;
import br.com.droneshop.compra.service.CarrinhoService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Carrinho anônimo pelo header X-Carrinho-Token (RF16, RF17, #24). */
@Path("carrinho")
@Produces(MediaType.APPLICATION_JSON)
public class CarrinhoResource {

    public static final String HEADER = "X-Carrinho-Token";

    @Inject
    CarrinhoService carrinhos;

    @Inject
    JsonWebToken token;

    private Long usuarioOuNulo() {
        return token == null || token.getSubject() == null ? null : Long.valueOf(token.getSubject());
    }

    @POST
    @PermitAll
    public CarrinhoResponseDTO criar() {
        return carrinhos.criar();
    }

    @GET
    @PermitAll
    public CarrinhoResponseDTO buscar(@HeaderParam(HEADER) String carrinho) {
        return carrinhos.buscar(carrinho);
    }

    @POST
    @Path("itens")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public CarrinhoResponseDTO adicionar(@HeaderParam(HEADER) String carrinho, @Valid ItemCarrinhoRequestDTO dto) {
        return carrinhos.adicionar(carrinho, dto);
    }

    @PATCH
    @Path("itens/{id}")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public CarrinhoResponseDTO alterar(@HeaderParam(HEADER) String carrinho, @PathParam("id") Long id,
            @Valid QuantidadeRequestDTO dto) {
        return carrinhos.alterarQuantidade(carrinho, id, dto.quantidade());
    }

    @DELETE
    @Path("itens/{id}")
    @PermitAll
    public CarrinhoResponseDTO remover(@HeaderParam(HEADER) String carrinho, @PathParam("id") Long id) {
        return carrinhos.remover(carrinho, id);
    }

    // Feito ao entrar no checkout (#23)
    @POST
    @Path("reservar")
    @PermitAll
    public CarrinhoResponseDTO reservar(@HeaderParam(HEADER) String carrinho) {
        return carrinhos.reservar(carrinho);
    }

    @POST
    @Path("cupom")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public CarrinhoResponseDTO aplicarCupom(@HeaderParam(HEADER) String carrinho, @Valid CupomCarrinhoRequestDTO dto) {
        return carrinhos.aplicarCupom(carrinho, dto.codigo(), usuarioOuNulo());
    }

    @DELETE
    @Path("cupom")
    @PermitAll
    public CarrinhoResponseDTO removerCupom(@HeaderParam(HEADER) String carrinho) {
        return carrinhos.removerCupom(carrinho);
    }

    @POST
    @Path("vincular")
    @RolesAllowed("CLIENTE")
    public CarrinhoResponseDTO vincular(@HeaderParam(HEADER) String carrinho) {
        return carrinhos.vincular(Long.valueOf(token.getSubject()), carrinho);
    }
}

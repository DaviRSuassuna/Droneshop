package br.com.droneshop.compra.resource;

import java.util.List;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.compra.dto.CompartilharResponseDTO;
import br.com.droneshop.compra.dto.ItemListaDesejosRequestDTO;
import br.com.droneshop.compra.dto.ItemListaDesejosResponseDTO;
import br.com.droneshop.compra.dto.MoverParaCarrinhoResponseDTO;
import br.com.droneshop.compra.service.FavoritosService;
import br.com.droneshop.compra.service.ListaDesejosService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Lista de desejos do cliente logado (RF19).
@Path("conta/desejos")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class ListaDesejosResource {

    @Inject
    UsuarioAtual usuarioAtual;

    @Inject
    ListaDesejosService desejos;

    @Inject
    FavoritosService favoritos;

    private Long usuario() {
        return usuarioAtual.id();
    }

    @GET
    public List<ItemListaDesejosResponseDTO> listar() {
        return desejos.listar(usuario());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adicionar(@Valid ItemListaDesejosRequestDTO dto) {
        desejos.adicionar(usuario(), dto.idProduto());
        return Response.noContent().build();
    }

    @DELETE
    @Path("{idProduto}")
    public Response remover(@PathParam("idProduto") Long idProduto) {
        desejos.remover(usuario(), idProduto);
        return Response.noContent().build();
    }

    @POST
    @Path("mover-para-carrinho")
    public MoverParaCarrinhoResponseDTO moverParaCarrinho() {
        return favoritos.moverParaCarrinho(usuario());
    }

    @POST
    @Path("compartilhar")
    public CompartilharResponseDTO compartilhar() {
        String t = favoritos.compartilhar(usuario());
        return new CompartilharResponseDTO(t, "/lista/" + t);
    }

    @DELETE
    @Path("compartilhar")
    public Response desligar() {
        favoritos.desligar(usuario());
        return Response.noContent().build();
    }
}

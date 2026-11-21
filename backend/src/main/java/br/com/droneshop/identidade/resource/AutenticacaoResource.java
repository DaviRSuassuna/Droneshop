package br.com.droneshop.identidade.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.compra.resource.CarrinhoResource;
import br.com.droneshop.compra.service.CarrinhoService;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.identidade.dto.LoginRequestDTO;
import br.com.droneshop.identidade.dto.LoginResponseDTO;
import br.com.droneshop.identidade.dto.PerfilAtivoRequestDTO;
import br.com.droneshop.identidade.dto.UsuarioResponseDTO;
import br.com.droneshop.identidade.service.AutenticacaoService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
public class AutenticacaoResource {

    @Inject
    AutenticacaoService autenticacao;

    @Inject
    JsonWebToken token;

    @Inject
    CarrinhoService carrinhos;

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    public LoginResponseDTO login(@Valid LoginRequestDTO dto, @HeaderParam(CarrinhoResource.HEADER) String carrinho) {
        LoginResponseDTO resposta = autenticacao.login(dto);
        // Login com o carrinho anônimo no header já vincula (RF17, #24)
        if (carrinho != null && !carrinho.isBlank() && resposta.perfis().contains("CLIENTE")) {
            try {
                carrinhos.vincular(resposta.usuario().id(), carrinho);
            } catch (ResourceNotFoundException e) {
                // token de carrinho desconhecido não impede o login
            }
        }
        return resposta;
    }

    @GET
    @Path("me")
    @Authenticated
    public UsuarioResponseDTO me() {
        return autenticacao.usuarioAtivo(Long.valueOf(token.getSubject()));
    }

    // Token de quem ainda vai escolher o perfil (sem groups) também chega aqui (#20).
    @POST
    @Path("perfil-ativo")
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    public LoginResponseDTO perfilAtivo(@Valid PerfilAtivoRequestDTO dto) {
        return autenticacao.escolherPerfil(Long.valueOf(token.getSubject()), SessaoAtual.id(token),
                dto.idPerfilAtivo());
    }

    @POST
    @Path("logout")
    @Authenticated
    public Response logout() {
        Long sid = SessaoAtual.id(token);
        if (sid != null)
            autenticacao.revogar(Long.valueOf(token.getSubject()), sid);
        return Response.noContent().build();
    }
}

package br.com.droneshop.identidade.resource;

import br.com.droneshop.identidade.dto.LoginRequestDTO;
import br.com.droneshop.identidade.dto.LoginResponseDTO;
import br.com.droneshop.identidade.dto.UsuarioResponseDTO;
import br.com.droneshop.identidade.service.AutenticacaoService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
public class AutenticacaoResource {

    @Inject
    AutenticacaoService autenticacao;

    @Inject
    JsonWebToken token;

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    public LoginResponseDTO login(@Valid LoginRequestDTO dto) {
        return autenticacao.login(dto);
    }

    @GET
    @Path("me")
    @Authenticated
    public UsuarioResponseDTO me() {
        return autenticacao.usuarioAtivo(Long.valueOf(token.getSubject()));
    }

    @POST
    @Path("logout")
    @Authenticated
    public Response logout() {
        return Response.noContent().build();
    }
}

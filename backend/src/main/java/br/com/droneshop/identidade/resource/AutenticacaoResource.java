package br.com.droneshop.identidade.resource;

import br.com.droneshop.administracao.service.AuditoriaInterceptor;
import br.com.droneshop.compra.resource.CarrinhoResource;
import br.com.droneshop.compra.service.CarrinhoService;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.identidade.dto.ClienteResponseDTO;
import br.com.droneshop.identidade.dto.ContaRequestDTO;
import br.com.droneshop.identidade.dto.LoginRequestDTO;
import br.com.droneshop.identidade.dto.LoginResponseDTO;
import br.com.droneshop.identidade.dto.RecuperarSenhaRequestDTO;
import br.com.droneshop.identidade.dto.UsuarioResponseDTO;
import br.com.droneshop.identidade.mapper.ClienteMapper;
import br.com.droneshop.identidade.service.AutenticacaoService;
import br.com.droneshop.identidade.service.AutenticacaoService.Sessao;
import br.com.droneshop.identidade.service.ClienteService;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
public class AutenticacaoResource {

    private static final String REFRESH = "aeria_refresh";

    @Inject
    AutenticacaoService autenticacao;

    @Inject
    ClienteService clientes;

    @Inject
    UsuarioAtual usuarioAtual;

    @Inject
    SecurityIdentity identidade;

    @Inject
    CarrinhoService carrinhos;

    @Inject
    @PersistenceUnitExtension
    AuditoriaInterceptor auditoria;

    @POST
    @Path("login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@NotNull @Valid LoginRequestDTO dto, @HeaderParam(CarrinhoResource.HEADER) String carrinho) {
        Sessao sessao = autenticacao.login(dto.email(), dto.senha(), dto.codigoOtp());
        if (sessao.papeis().contains("ADMINISTRATIVO"))
            auditoria.registrarLogin(sessao.usuario());
        if (carrinho != null && !carrinho.isBlank() && sessao.papeis().contains("CLIENTE")) {
            try {
                carrinhos.vincular(sessao.usuario().getId(), carrinho);
            } catch (ResourceNotFoundException e) {
                // Um carrinho expirado não impede o login.
            }
        }
        return comSessao(sessao);
    }

    @POST
    @Path("cadastro")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ClienteResponseDTO cadastro(@NotNull @Valid ContaRequestDTO dto) {
        return ClienteMapper.toConta(clientes.criarConta(dto));
    }

    @GET
    @Path("eu")
    @RolesAllowed({"CLIENTE", "ADMINISTRATIVO", "ADMIN"})
    public UsuarioResponseDTO eu() {
        var usuario = usuarioAtual.usuario();
        var papeis = AutenticacaoService.PAPEIS.stream().filter(identidade::hasRole).toList();
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), papeis);
    }

    @POST
    @Path("renovar")
    @PermitAll
    public Response renovar(@CookieParam(REFRESH) String refresh) {
        return comSessao(autenticacao.renovar(refresh));
    }

    @POST
    @Path("recuperar-senha")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recuperarSenha(@NotNull @Valid RecuperarSenhaRequestDTO dto) {
        autenticacao.recuperarSenha(dto.email());
        return Response.noContent().build();
    }

    @POST
    @Path("sair")
    @PermitAll
    public Response sair(@CookieParam(REFRESH) String refresh) {
        autenticacao.sair(refresh);
        return Response.noContent().cookie(cookie("", 0)).build();
    }

    private static Response comSessao(Sessao sessao) {
        var tokens = sessao.tokens();
        return Response.ok(new LoginResponseDTO(tokens.accessToken(), tokens.expiraEm(), sessao.papeis()))
                .cookie(cookie(tokens.refreshToken(), Math.toIntExact(tokens.refreshExpiraEm())))
                .header("Cache-Control", "no-store")
                .build();
    }

    private static NewCookie cookie(String valor, int duracao) {
        return new NewCookie.Builder(REFRESH).value(valor).path("/auth")
                .httpOnly(true).secure(true).sameSite(NewCookie.SameSite.STRICT).maxAge(duracao).build();
    }
}

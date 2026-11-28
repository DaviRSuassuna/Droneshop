package br.com.droneshop.identidade.service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.droneshop.exception.ServicoExternoIndisponivelException;
import br.com.droneshop.identidade.client.KeycloakClient;
import br.com.droneshop.identidade.client.KeycloakClient.Tokens;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AutenticacaoService {

    public static final List<String> PAPEIS = List.of("CLIENTE", "ADMINISTRATIVO", "ADMIN");

    @Inject
    KeycloakClient keycloak;

    @Inject
    UsuarioRepository usuarios;

    @Inject
    ObjectMapper json;

    public record Sessao(Tokens tokens, Usuario usuario, List<String> papeis) {
    }

    public Sessao login(String email, String senha, String otp) {
        return sessao(keycloak.login(email, senha, otp));
    }

    public Sessao renovar(String refresh) {
        return sessao(keycloak.renovar(refresh));
    }

    public void sair(String refresh) {
        keycloak.sair(refresh);
    }

    public void recuperarSenha(String email) {
        Usuario usuario = usuarios.porEmail(email);
        if (usuario != null && usuario.isAtivo())
            keycloak.enviarAcao(usuario.getKeycloakSub(), "UPDATE_PASSWORD");
    }

    private Sessao sessao(Tokens tokens) {
        // Token recebido diretamente do endpoint OIDC configurado. O resource server valida
        // assinatura, emissor e audiência a cada chamada autenticada.
        try {
            JsonNode claims = json.readTree(Base64.getUrlDecoder().decode(tokens.accessToken().split("\\.")[1]));
            Usuario usuario = usuarios.porSub(claims.path("sub").asText());
            if (usuario == null || !usuario.isAtivo()) {
                keycloak.sair(tokens.refreshToken());
                throw new KeycloakClient.CredenciaisInvalidas(false);
            }
            JsonNode roles = claims.path("realm_access").path("roles");
            List<String> papeis = PAPEIS.stream()
                    .filter(papel -> StreamSupport.stream(roles.spliterator(), false).anyMatch(role -> papel.equals(role.asText())))
                    .toList();
            return new Sessao(tokens, usuario, papeis);
        } catch (IOException | IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new ServicoExternoIndisponivelException("Resposta inválida do serviço de identidade.", e);
        }
    }
}

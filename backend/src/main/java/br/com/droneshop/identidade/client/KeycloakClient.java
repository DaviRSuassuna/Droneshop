package br.com.droneshop.identidade.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.droneshop.exception.ServicoExternoIndisponivelException;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

@ApplicationScoped
public class KeycloakClient {

    public record Tokens(String accessToken, String refreshToken, long expiraEm, long refreshExpiraEm) {
    }

    public static class CredenciaisInvalidas extends RuntimeException {
        public final boolean otpNecessario;

        public CredenciaisInvalidas(boolean otpNecessario) {
            this.otpNecessario = otpNecessario;
        }
    }

    private static final Logger LOG = Logger.getLogger(KeycloakClient.class);
    private static final String CLIENTE_OIDC = "aeria-api";
    private static final String OIDC = "/protocol/openid-connect/";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    @Inject
    ObjectMapper json;

    @Inject
    TransactionSynchronizationRegistry transacao;

    @ConfigProperty(name = "aeria.keycloak.realm")
    String realm;

    @ConfigProperty(name = "aeria.keycloak.segredo")
    String segredo;

    @ConfigProperty(name = "aeria.loja.url", defaultValue = "http://localhost:4200")
    String loja;

    public Tokens login(String email, String senha, String otp) {
        Map<String, String> dados = new HashMap<>(Map.of("grant_type", "password",
                "username", email.strip().toLowerCase(Locale.ROOT), "password", senha));
        if (otp != null && !otp.isBlank()) dados.put("otp", otp.strip());
        return tokens(formulario("token", dados));
    }

    public Tokens renovar(String refresh) {
        if (refresh == null || refresh.isBlank()) throw new CredenciaisInvalidas(false);
        return tokens(formulario("token", Map.of("grant_type", "refresh_token", "refresh_token", refresh)));
    }

    public void sair(String refresh) {
        if (refresh == null || refresh.isBlank()) return;
        var logout = formulario("logout", Map.of("refresh_token", refresh));
        var revogacao = formulario("revoke", Map.of("token", refresh, "token_type_hint", "refresh_token"));
        // O refresh pode já estar inválido e ainda deve passar pela revogação.
        if (logout.statusCode() != 400 && logout.statusCode() != 401) conferir(logout);
        conferir(revogacao);
    }

    public boolean confirmarSenha(String email, String senha) {
        if (senha == null) return false;
        try {
            sair(login(email, senha, null).refreshToken());
            return true;
        } catch (CredenciaisInvalidas e) {
            return e.otpNecessario;
        }
    }

    public String criar(String nome, String email, String senha, boolean ativo, boolean verificado, String papel) {
        String admin = tokenAdmin();
        String normalizado = email.strip().toLowerCase(Locale.ROOT);
        var resposta = admin(admin, "POST", "/users", Map.of(
                "username", normalizado, "email", normalizado, "firstName", nome, "lastName", nome,
                "enabled", ativo, "emailVerified", verificado, "credentials", List.of(credencial(senha))));
        if (resposta.statusCode() == 409)
            throw ValidationException.of("email", "Já existe uma conta com esse e-mail.");
        conferir(resposta);
        String local = resposta.headers().firstValue("Location").orElseThrow(KeycloakClient::indisponivel);
        String caminho = URI.create(local).getPath();
        String sub = caminho.substring(caminho.lastIndexOf('/') + 1);
        try {
            atribuirPapel(admin, sub, papel);
            removerSeCadastroReverter(sub);
            return sub;
        } catch (RuntimeException falha) {
            try {
                conferir(admin(admin, "DELETE", usuario(sub), null));
            } catch (RuntimeException limpeza) {
                falha.addSuppressed(limpeza);
            }
            throw falha;
        }
    }

    private void removerSeCadastroReverter(String sub) {
        transacao.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
            }

            @Override
            public void afterCompletion(int status) {
                if (status != Status.STATUS_ROLLEDBACK) return;
                try {
                    remover(sub);
                } catch (RuntimeException e) {
                    LOG.error("Falha ao remover cadastro incompleto no Keycloak.", e);
                }
            }
        });
    }

    public void remover(String sub) {
        conferir(admin(tokenAdmin(), "DELETE", usuario(sub), null));
    }

    public void atualizar(String sub, String nome, String email, boolean ativo) {
        conferir(admin(tokenAdmin(), "PUT", usuario(sub),
                Map.of("firstName", nome, "lastName", nome, "username", email, "email", email, "enabled", ativo)));
    }

    public void papelAdministrativo(String sub, String papel) {
        String admin = tokenAdmin();
        atribuirPapel(admin, sub, papel);
        String anterior = "ADMIN".equals(papel) ? "ADMINISTRATIVO" : "ADMIN";
        conferir(admin(admin, "DELETE", usuario(sub) + "/role-mappings/realm", List.of(papel(admin, anterior))));
    }

    public boolean temOtp(String sub) {
        var credenciais = corpo(conferir(admin(tokenAdmin(), "GET", usuario(sub) + "/credentials", null)));
        return StreamSupport.stream(credenciais.spliterator(), false)
                .anyMatch(credencial -> "otp".equals(credencial.path("type").asText()));
    }

    public void trocarSenha(String sub, String senha) {
        conferir(admin(tokenAdmin(), "PUT", usuario(sub) + "/reset-password", credencial(senha)));
    }

    public void enviarAcao(String sub, String acao) {
        // Depois da ação no Keycloak, o link "Voltar" leva de volta ao login da loja.
        String volta = "?client_id=" + CLIENTE_OIDC + "&redirect_uri=" + codificar(loja + "/entrar");
        conferir(admin(tokenAdmin(), "PUT", usuario(sub) + "/execute-actions-email" + volta, List.of(acao)));
    }

    public void revogarSessoes(String sub) {
        conferir(admin(tokenAdmin(), "POST", usuario(sub) + "/logout", null));
    }

    private void atribuirPapel(String admin, String sub, String papel) {
        conferir(admin(admin, "POST", usuario(sub) + "/role-mappings/realm", List.of(papel(admin, papel))));
    }

    private JsonNode papel(String admin, String papel) {
        return corpo(conferir(admin(admin, "GET", "/roles/" + papel, null)));
    }

    private static Map<String, Object> credencial(String senha) {
        return Map.of("type", "password", "value", senha, "temporary", false);
    }

    private static String usuario(String sub) {
        return "/users/" + codificar(sub);
    }

    private String tokenAdmin() {
        return corpo(conferir(formulario("token", Map.of("grant_type", "client_credentials"))))
                .path("access_token").asText();
    }

    private Tokens tokens(HttpResponse<String> resposta) {
        if (resposta.statusCode() == 400 || resposta.statusCode() == 401) {
            String erro = corpo(resposta).path("error").asText();
            if (List.of("invalid_grant", "otp-necessario").contains(erro))
                throw new CredenciaisInvalidas("otp-necessario".equals(erro));
        }
        JsonNode token = corpo(conferir(resposta));
        return new Tokens(token.path("access_token").asText(), token.path("refresh_token").asText(),
                token.path("expires_in").asLong(), token.path("refresh_expires_in").asLong());
    }

    private HttpResponse<String> formulario(String operacao, Map<String, String> dados) {
        Map<String, String> formulario = new HashMap<>(dados);
        formulario.put("client_id", CLIENTE_OIDC);
        formulario.put("client_secret", segredo);
        String conteudo = formulario.entrySet().stream()
                .map(e -> codificar(e.getKey()) + "=" + codificar(e.getValue()))
                .collect(Collectors.joining("&"));
        return enviar(requisicao(realm + OIDC + operacao)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(conteudo)));
    }

    private HttpResponse<String> admin(String token, String metodo, String caminho, Object dados) {
        String raiz = realm.substring(0, realm.lastIndexOf("/realms/"));
        String nomeRealm = realm.substring(realm.lastIndexOf('/') + 1);
        try {
            var requisicao = requisicao(raiz + "/admin/realms/" + nomeRealm + caminho)
                    .header("Authorization", "Bearer " + token);
            if (dados == null)
                return enviar(requisicao.method(metodo, HttpRequest.BodyPublishers.noBody()));
            return enviar(requisicao.header("Content-Type", "application/json")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(json.writeValueAsString(dados))));
        } catch (IOException e) {
            throw indisponivel();
        }
    }

    private static HttpRequest.Builder requisicao(String url) {
        return HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8));
    }

    private HttpResponse<String> enviar(HttpRequest.Builder requisicao) {
        try {
            return http.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw indisponivel();
        } catch (IOException e) {
            throw indisponivel();
        }
    }

    private JsonNode corpo(HttpResponse<String> resposta) {
        try {
            return json.readTree(resposta.body());
        } catch (IOException e) {
            throw indisponivel();
        }
    }

    private static HttpResponse<String> conferir(HttpResponse<String> resposta) {
        if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) throw indisponivel();
        return resposta;
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private static ServicoExternoIndisponivelException indisponivel() {
        return new ServicoExternoIndisponivelException("Serviço de identidade indisponível.", null);
    }
}

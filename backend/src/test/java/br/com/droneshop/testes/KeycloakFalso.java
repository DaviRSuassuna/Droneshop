package br.com.droneshop.testes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class KeycloakFalso implements QuarkusTestResourceLifecycleManager {

    private static final String REALM = "/realms/aeria";
    private static final String OIDC = REALM + "/protocol/openid-connect/";
    private static final String ADMIN = "/admin" + REALM;
    private final ObjectMapper json = new ObjectMapper();
    private static final Map<String, List<String>> EMAILS = new ConcurrentHashMap<>();
    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();
    private final Map<String, String> sessoes = new ConcurrentHashMap<>();
    private HttpServer servidor;
    private KeyPair chaves;
    private String emissor;

    private static class Usuario {
        final String sub;
        final Set<String> papeis = new HashSet<>();
        String email;
        String senha;
        boolean ativo = true;
        boolean otp;

        Usuario(String sub, String email, String senha, String... papeis) {
            this.sub = sub;
            this.email = email;
            this.senha = senha;
            this.papeis.addAll(List.of(papeis));
        }
    }

    @Override
    public Map<String, String> start() {
        try {
            EMAILS.clear();
            var gerador = KeyPairGenerator.getInstance("RSA");
            gerador.initialize(2048);
            chaves = gerador.generateKeyPair();
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            emissor = "http://localhost:" + servidor.getAddress().getPort() + REALM;
            var admin = new Usuario("aeria-admin", "admin@aeria.com.br", "aeria123", "ADMIN");
            var cliente = new Usuario("aeria-cliente", "cliente@aeria.com.br", "aeria123", "CLIENTE");
            var otp = new Usuario("aeria-admin-otp", "otp@aeria.com.br", "aeria123", "ADMIN");
            otp.otp = true;
            for (Usuario usuario : List.of(admin, cliente, otp)) usuarios.put(usuario.sub, usuario);
            servidor.createContext("/", this::atender);
            servidor.start();
            return Map.of("quarkus.oidc.auth-server-url", emissor, "aeria.keycloak.segredo", "segredo-falso");
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao subir o Keycloak falso.", e);
        }
    }

    @Override
    public void stop() {
        if (servidor != null) servidor.stop(0);
    }

    public static List<String> acoesEnviadas(String email) {
        return EMAILS.getOrDefault(email, List.of());
    }

    private synchronized void atender(HttpExchange ex) throws IOException {
        try (ex) {
            try {
                encaminhar(ex);
            } catch (Exception e) {
                responder(ex, 500, Map.of("error", e.getClass().getSimpleName()));
            }
        }
    }

    private void encaminhar(HttpExchange ex) throws Exception {
        String caminho = ex.getRequestURI().getPath();
        if (caminho.equals(REALM + "/.well-known/openid-configuration")) {
            String oidc = emissor + "/protocol/openid-connect/";
            responder(ex, 200, Map.of("issuer", emissor, "jwks_uri", oidc + "certs",
                    "token_endpoint", oidc + "token", "introspection_endpoint", oidc + "token/introspect",
                    "end_session_endpoint", oidc + "logout"));
        } else if (caminho.equals(OIDC + "certs")) {
            var rsa = (RSAPublicKey) chaves.getPublic();
            responder(ex, 200, Map.of("keys", List.of(Map.of("kid", "teste", "kty", "RSA", "alg", "RS256",
                    "use", "sig", "n", inteiro(rsa.getModulus().toByteArray()),
                    "e", inteiro(rsa.getPublicExponent().toByteArray())))));
        } else if (caminho.startsWith(OIDC)) {
            oidc(ex, caminho.substring(OIDC.length()));
        } else if (caminho.startsWith(ADMIN + "/")) {
            if (!"Bearer admin-falso".equals(ex.getRequestHeaders().getFirst("Authorization"))) {
                responder(ex, 403, null);
                return;
            }
            administrar(ex, caminho.substring(ADMIN.length() + 1));
        } else {
            responder(ex, 404, null);
        }
    }

    private void oidc(HttpExchange ex, String operacao) throws Exception {
        Map<String, String> form = new HashMap<>();
        String entrada = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        for (String par : entrada.split("&")) {
            String[] campos = par.split("=", 2);
            if (campos.length == 2) form.put(campos[0], URLDecoder.decode(campos[1], StandardCharsets.UTF_8));
        }
        if (!"aeria-api".equals(form.get("client_id")) || !"segredo-falso".equals(form.get("client_secret"))) {
            responder(ex, 401, Map.of("error", "invalid_client"));
            return;
        }
        switch (operacao) {
            case "token" -> emitir(ex, form);
            case "logout" -> {
                sessoes.remove(form.get("refresh_token"));
                responder(ex, 204, null);
            }
            case "revoke" -> {
                sessoes.remove(form.get("token"));
                responder(ex, 200, null);
            }
            default -> responder(ex, 404, null);
        }
    }

    private void emitir(HttpExchange ex, Map<String, String> form) throws Exception {
        String grant = form.get("grant_type");
        if ("client_credentials".equals(grant)) {
            responder(ex, 200, Map.of("access_token", "admin-falso"));
            return;
        }
        Usuario usuario;
        if ("refresh_token".equals(grant)) {
            String sub = sessoes.remove(form.get("refresh_token"));
            usuario = sub == null ? null : usuarios.get(sub);
        } else {
            usuario = porEmail(form.get("username"));
            if (usuario != null && !usuario.senha.equals(form.get("password"))) usuario = null;
        }
        if (usuario == null || !usuario.ativo) {
            responder(ex, 400, Map.of("error", "invalid_grant"));
            return;
        }
        if ("password".equals(grant) && usuario.otp && !"123456".equals(form.get("otp"))) {
            responder(ex, 400, Map.of("error", form.containsKey("otp") ? "invalid_grant" : "otp-necessario"));
            return;
        }

        long agora = Instant.now().getEpochSecond();
        Set<String> papeis = new HashSet<>(usuario.papeis);
        if (papeis.contains("ADMIN")) papeis.add("ADMINISTRATIVO");
        String cabecalho = base64(json.writeValueAsBytes(Map.of("kid", "teste", "alg", "RS256", "typ", "JWT")));
        String claims = base64(json.writeValueAsBytes(Map.of("iss", emissor, "sub", usuario.sub, "aud", "aeria-api",
                "iat", agora, "exp", agora + 300, "typ", "Bearer", "preferred_username", usuario.email,
                "realm_access", Map.of("roles", papeis))));
        String mensagem = cabecalho + "." + claims;
        Signature assinatura = Signature.getInstance("SHA256withRSA");
        assinatura.initSign(chaves.getPrivate());
        assinatura.update(mensagem.getBytes(StandardCharsets.US_ASCII));
        String refresh = UUID.randomUUID().toString();
        sessoes.put(refresh, usuario.sub);
        responder(ex, 200, Map.of("access_token", mensagem + "." + base64(assinatura.sign()),
                "refresh_token", refresh, "expires_in", 300, "refresh_expires_in", 1800));
    }

    private void administrar(HttpExchange ex, String caminho) throws IOException {
        String metodo = ex.getRequestMethod();
        String[] partes = caminho.split("/", 3);
        JsonNode dados = json.readTree(ex.getRequestBody());
        if (partes[0].equals("roles") && partes.length == 2) {
            responder(ex, 200, Map.of("id", partes[1], "name", partes[1]));
            return;
        }
        if (caminho.equals("users") && metodo.equals("POST")) {
            String email = dados.path("email").asText();
            if (porEmail(email) != null) {
                responder(ex, 409, null);
                return;
            }
            String sub = UUID.randomUUID().toString();
            Usuario usuario = new Usuario(sub, email, dados.path("credentials").get(0).path("value").asText());
            usuario.ativo = dados.path("enabled").asBoolean();
            usuarios.put(sub, usuario);
            ex.getResponseHeaders().set("Location", ADMIN + "/users/" + sub);
            responder(ex, 201, null);
            return;
        }
        Usuario usuario = partes.length < 2 ? null : usuarios.get(partes[1]);
        if (usuario == null) {
            responder(ex, 404, null);
            return;
        }
        String acao = partes.length == 3 ? partes[2] : "";
        switch (acao) {
            case "" -> {
                if ("DELETE".equals(metodo)) {
                    usuarios.remove(usuario.sub);
                    sessoes.values().removeIf(usuario.sub::equals);
                } else {
                    usuario.email = dados.path("email").asText();
                    usuario.ativo = dados.path("enabled").asBoolean();
                }
            }
            case "role-mappings/realm" -> {
                String papel = dados.get(0).path("name").asText();
                if ("DELETE".equals(metodo)) usuario.papeis.remove(papel);
                else usuario.papeis.add(papel);
            }
            case "execute-actions-email" -> EMAILS.compute(usuario.email, (email, acoes) -> {
                var novas = acoes == null ? new ArrayList<String>() : new ArrayList<>(acoes);
                novas.add(dados.get(0).asText());
                return List.copyOf(novas);
            });
            case "reset-password" -> usuario.senha = dados.path("value").asText();
            case "logout" -> sessoes.values().removeIf(usuario.sub::equals);
            case "credentials" -> {
                responder(ex, 200, usuario.otp ? List.of(Map.of("type", "otp")) : List.of());
                return;
            }
            default -> {
                responder(ex, 404, null);
                return;
            }
        }
        responder(ex, 204, null);
    }

    private Usuario porEmail(String email) {
        return usuarios.values().stream().filter(usuario -> usuario.email.equals(email)).findFirst().orElse(null);
    }

    private void responder(HttpExchange ex, int status, Object corpo) throws IOException {
        if (corpo == null) {
            ex.sendResponseHeaders(status, -1);
            return;
        }
        byte[] bytes = json.writeValueAsBytes(corpo);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
    }

    private static String inteiro(byte[] bytes) {
        return base64(bytes[0] == 0 ? Arrays.copyOfRange(bytes, 1, bytes.length) : bytes);
    }

    private static String base64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

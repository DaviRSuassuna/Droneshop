package br.com.droneshop.identidade;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #33 Verificação de e-mail e recuperação de senha por token (RF49).
 * Contrato: docs/issues/033.md. O token é lido do e-mail guardado pelo ResendFalso.
 */
@QuarkusTest
@Tag("issue-33")
class TokenEmailResourceTest {

    static final Pattern TOKEN = Pattern.compile("token=([A-Za-z0-9_\\-]+)");

    /** Token do e-mail mais recente enviado para o endereço. */
    static String tokenDoUltimoEmail(String email) {
        String html = anonimo().queryParam("para", email).when().get("/resend-falso/emails")
                .then().statusCode(200).extract().jsonPath().getString("[0].html");
        Matcher m = TOKEN.matcher(html);
        org.junit.jupiter.api.Assertions.assertTrue(m.find(), "o e-mail deveria trazer token=...");
        return m.group(1);
    }

    static void recuperar(String email) {
        anonimo().contentType(ContentType.JSON).body(Map.of("email", email))
                .when().post("/auth/recuperar-senha").then().statusCode(204);
    }

    @Test
    void verificaOEmailComOToken() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).when().post("/auth/verificar-email/enviar").then().statusCode(204);
        String token = tokenDoUltimoEmail(c.email());
        anonimo().contentType(ContentType.JSON).body(Map.of("token", token))
                .when().post("/auth/verificar-email").then().statusCode(204);
        anonimo().contentType(ContentType.JSON).body(Map.of("token", token))
                .when().post("/auth/verificar-email")
                .then().statusCode(400).body("errors.field", hasItem("token")); // uso único
    }

    @Test
    void redefineASenhaERevogaAsSessoes() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        recuperar(c.email());
        String token = tokenDoUltimoEmail(c.email());
        anonimo().contentType(ContentType.JSON).body(Map.of("token", token, "senhaNova", "novaSenha123"))
                .when().post("/auth/redefinir-senha").then().statusCode(204);
        Api.tentarLogin(c.email(), "novaSenha123").then().statusCode(200);
        Api.tentarLogin(c.email(), c.senha()).then().statusCode(401);
        como(c.token()).when().get("/conta/perfil").then().statusCode(401);
    }

    @Test
    void tokenInvalidoResponde400() {
        anonimo().contentType(ContentType.JSON).body(Map.of("token", "inventado", "senhaNova", "novaSenha123"))
                .when().post("/auth/redefinir-senha")
                .then().statusCode(400).body("errors.field", hasItem("token"));
    }

    @Test
    void emailDesconhecidoTambemResponde204SemEnviar() {
        String email = "ninguem-" + System.nanoTime() + "@cliente.com.br";
        recuperar(email);
        anonimo().queryParam("para", email).when().get("/resend-falso/emails")
                .then().statusCode(200).body("$", hasSize(0));
    }
}

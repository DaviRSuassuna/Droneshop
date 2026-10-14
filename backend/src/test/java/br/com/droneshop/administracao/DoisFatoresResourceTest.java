package br.com.droneshop.administracao;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import br.com.droneshop.testes.Totp;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #45 2FA opcional no perfil administrativo (RF44, RNF02).
 * Contrato: docs/issues/045.md. Usa um administrador próprio, nunca o semente.
 */
@QuarkusTest
@Tag("issue-45")
class DoisFatoresResourceTest {

    record Admin(String email, String token) {
    }

    static Admin novoAdmin() {
        String email = Dados.sufixo() + "@aeria.com.br";
        Map<String, Object> u = new HashMap<>();
        u.put("nome", "Admin 2FA");
        u.put("email", email);
        u.put("senha", "provisoria123");
        u.put("idPapel", Fixtures.PAPEL_ADMIN);
        u.put("ativo", true);
        given().contentType(ContentType.JSON).body(u).when().post("/admin/usuarios").then().statusCode(200);
        return new Admin(email, Api.login(email, "provisoria123"));
    }

    /** Liga o 2FA e devolve o secret. */
    static String ligar(Admin a) {
        String secret = como(a.token()).when().post("/conta/2fa")
                .then().statusCode(200)
                .body("otpauthUrl", startsWith("otpauth://totp/"))
                .extract().jsonPath().getString("secret");
        como(a.token()).contentType(ContentType.JSON).body(Map.of("codigo", Totp.codigo(secret)))
                .when().post("/conta/2fa/confirmar").then().statusCode(204);
        return secret;
    }

    @Test
    void codigoErradoNaoLiga() {
        Admin a = novoAdmin();
        como(a.token()).when().post("/conta/2fa").then().statusCode(200);
        como(a.token()).contentType(ContentType.JSON).body(Map.of("codigo", "000000"))
                .when().post("/conta/2fa/confirmar")
                .then().statusCode(400).body("errors.field", hasItem("codigo"));
    }

    @Test
    void loginPassaAPedirOCodigo() {
        Admin a = novoAdmin();
        String secret = ligar(a);
        String temporario = Api.tentarLogin(a.email(), "provisoria123").then().statusCode(200)
                .body("precisa2fa", equalTo(true))
                .body("token", nullValue())
                .extract().jsonPath().getString("tokenTemporario");
        anonimo().contentType(ContentType.JSON).body(Map.of("tokenTemporario", temporario, "codigo", "000000"))
                .when().post("/auth/login/2fa").then().statusCode(401);
        anonimo().contentType(ContentType.JSON)
                .body(Map.of("tokenTemporario", temporario, "codigo", Totp.codigo(secret)))
                .when().post("/auth/login/2fa")
                .then().statusCode(200).body("token", notNullValue());
    }

    @Test
    void secretNuncaVoltaDepoisDeLigado() {
        Admin a = novoAdmin();
        String secret = ligar(a);
        como(a.token()).when().post("/conta/2fa").then().statusCode(409);
        como(a.token()).when().get("/auth/me").then().statusCode(200).body(not(containsString(secret)));
        given().queryParam("nome", "Admin 2FA").when().get("/admin/usuarios")
                .then().statusCode(200).body(not(containsString(secret)));
    }

    @Test
    void desligaComCodigoValido() {
        Admin a = novoAdmin();
        String secret = ligar(a);
        como(a.token()).contentType(ContentType.JSON).body(Map.of("codigo", Totp.codigo(secret)))
                .when().delete("/conta/2fa").then().statusCode(204);
        Api.tentarLogin(a.email(), "provisoria123").then().statusCode(200).body("token", notNullValue());
    }
}

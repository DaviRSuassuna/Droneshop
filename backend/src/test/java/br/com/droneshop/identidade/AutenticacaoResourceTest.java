package br.com.droneshop.identidade;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import io.restassured.http.ContentType;

import java.time.Duration;
import java.util.Set;

/**
 * #4 Usuario: login por e-mail e senha com JWT, rotas do admin protegidas.
 * Contrato: docs/issues/004.md
 */
@QuarkusTest
@Tag("issue-4")
class AutenticacaoResourceTest {

    @Test
    void loginDoAdminSementeDevolveTokenEPerfil() {
        Api.tentarLogin(Api.ADMIN_EMAIL, Api.ADMIN_SENHA)
                .then().statusCode(200)
                .body("token", notNullValue())
                .body("expiraEm", notNullValue())
                .body("usuario.email", equalTo(Api.ADMIN_EMAIL))
                .body("perfis", hasItem("ADMINISTRATIVO"))
                .body(not(containsString("senha")));
    }

    @Test
    void emailNaoDiferenciaMaiusculas() {
        Api.tentarLogin(Api.ADMIN_EMAIL.toUpperCase(), Api.ADMIN_SENHA)
                .then().statusCode(200);
    }

    @Test
    void senhaErradaEEmailDesconhecidoRespondemOMesmo401() {
        String erroSenha = Api.tentarLogin(Api.ADMIN_EMAIL, "senha-errada")
                .then().statusCode(401)
                .contentType("application/problem+json")
                .body("type", endsWith("/errors/unauthorized"))
                .extract().jsonPath().getString("detail");
        String erroEmail = Api.tentarLogin("ninguem@aeria.com.br", "senha-errada")
                .then().statusCode(401)
                .extract().jsonPath().getString("detail");
        org.junit.jupiter.api.Assertions.assertEquals(erroSenha, erroEmail,
                "a mensagem não pode revelar se o e-mail existe");
    }

    @Test
    void loginPorCpfNaoFunciona() {
        Api.tentarLogin("52998224725", Api.ADMIN_SENHA).then().statusCode(401);
    }

    @Test
    void camposVaziosRespondem400() {
        anonimo().contentType(ContentType.JSON).body(Map.of("email", "", "senha", ""))
                .when().post("/auth/login")
                .then().statusCode(400)
                .body("errors.field", hasItems("email", "senha"));
    }

    @Test
    void meDevolveOUsuarioDoToken() {
        String token = Api.login(Api.ADMIN_EMAIL, Api.ADMIN_SENHA);
        como(token).when().get("/auth/me")
                .then().statusCode(200)
                .body("email", equalTo(Api.ADMIN_EMAIL))
                .body("perfis", hasItem("ADMINISTRATIVO"))
                .body(not(containsString("senhaHash")));
    }

    @Test
    void meSemTokenResponde401() {
        anonimo().when().get("/auth/me").then().statusCode(401);
    }

    @Test
    void tokenInvalidoResponde401() {
        como("isto-nao-e-um-jwt").when().get("/auth/me").then().statusCode(401);
    }

    @Test
    void rotasDoAdminExigemToken() {
        anonimo().when().get("/marcas").then().statusCode(401);
        anonimo().contentType(ContentType.JSON).body(Map.of("idStatus", 1))
                .when().post("/marcas").then().statusCode(401);
        anonimo().when().get("/fornecedores").then().statusCode(401);
        anonimo().when().get("/produtos").then().statusCode(401);
    }

    @Test
    void usuarioSemPerfilAdministrativoRecebe403Problem() {
        String token = Jwt.issuer("droneshop-api")
                .subject("999")
                .upn("cliente@aeria.com.br")
                .groups(Set.of("CLIENTE"))
                .expiresIn(Duration.ofMinutes(5))
                .sign();
        como(token).when().get("/marcas")
                .then().statusCode(403)
                .contentType("application/problem+json")
                .body("type", endsWith("/errors/forbidden"));
    }

    @Test
    void rotasPublicasContinuamAbertas() {
        anonimo().when().get("/estados").then().statusCode(200);
        anonimo().when().get("/status-marca").then().statusCode(200);
        anonimo().when().get("/viacep-falso/ws/77001002/json/").then().statusCode(200);
    }

    @Test
    void logoutResponde204() {
        String token = Api.login(Api.ADMIN_EMAIL, Api.ADMIN_SENHA);
        como(token).when().post("/auth/logout").then().statusCode(204);
    }
}

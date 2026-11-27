package br.com.droneshop.identidade;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import br.com.droneshop.testes.KeycloakFalso;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@Tag("issue-4")
class AutenticacaoResourceTest {

    @Test
    void loginAdminDevolveTokenPapeisECookieProtegido() {
        Api.tentarLogin(Api.ADMIN_EMAIL.toUpperCase(), Api.ADMIN_SENHA)
                .then().statusCode(200)
                .body("accessToken", notNullValue())
                .body("expiraEm", equalTo(300))
                .body("papeis", hasItems("ADMIN", "ADMINISTRATIVO"))
                .body(not(containsString("refreshToken")))
                .header("Set-Cookie", containsString("HttpOnly"))
                .header("Set-Cookie", containsString("Secure"))
                .header("Set-Cookie", containsString("SameSite=Strict"));
    }

    @Test
    void senhaErradaEEmailDesconhecidoRespondemOMesmo401() {
        String erroSenha = Api.tentarLogin(Api.ADMIN_EMAIL, "senha-errada")
                .then().statusCode(401).contentType("application/problem+json")
                .body("type", endsWith("/errors/unauthorized")).extract().path("detail");
        String erroEmail = Api.tentarLogin("ninguem@aeria.com.br", "senha-errada")
                .then().statusCode(401).extract().path("detail");
        assertEquals(erroSenha, erroEmail);
        Api.tentarLogin("52998224725", Api.ADMIN_SENHA).then().statusCode(401);
    }

    @Test
    void camposVaziosRespondem400() {
        anonimo().contentType(ContentType.JSON).body(Map.of("email", "", "senha", ""))
                .when().post("/auth/login").then().statusCode(400).body("errors.field", hasItems("email", "senha"));
    }

    @Test
    void euDevolveOUsuarioDoToken() {
        String token = Api.login(Api.ADMIN_EMAIL, Api.ADMIN_SENHA);
        como(token).when().get("/auth/eu").then().statusCode(200)
                .body("id", notNullValue()).body("nome", notNullValue())
                .body("email", equalTo(Api.ADMIN_EMAIL))
                .body("papeis", hasItems("ADMIN", "ADMINISTRATIVO"))
                .body(not(containsString("senhaHash")));
    }

    @Test
    void euRejeitaCredenciaisInvalidas() {
        anonimo().when().get("/auth/eu").then().statusCode(401).contentType("application/problem+json");
        como("isto-nao-e-um-jwt").when().get("/auth/eu").then().statusCode(401)
                .contentType("application/problem+json").body("type", endsWith("/errors/unauthorized"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/marcas", "/fornecedores", "/produtos", "/admin/usuarios", "/estoque",
            "/pedidos", "/admin/devolucoes", "/admin/chamados", "/admin/templates-email"})
    void rotasAdministrativasRejeitamAnonimoECliente(String rota) {
        proteger(rota, Api.login(Api.CLIENTE_EMAIL, Api.CLIENTE_SENHA));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/conta/perfil", "/conta/carrinho", "/conta/cartoes"})
    void rotasDoClienteRejeitamAnonimoEAdmin(String rota) {
        proteger(rota, Api.tokenAdmin());
    }

    private void proteger(String rota, String token) {
        anonimo().when().get(rota).then().statusCode(401)
                .contentType("application/problem+json").body("type", endsWith("/errors/unauthorized"));
        como(token).when().get(rota).then().statusCode(403)
                .contentType("application/problem+json").body("type", endsWith("/errors/forbidden"));
    }

    @Test
    void rotasPublicasContinuamAbertas() {
        for (String rota : Arrays.asList("/estados", "/status-marca", "/viacep-falso/ws/77001002/json/"))
            anonimo().when().get(rota).then().statusCode(200);
    }

    @Test
    void otpSoEPedidoDepoisDeValidarSenha() {
        String email = "otp@aeria.com.br";
        Api.tentarLogin(email, "errada").then().statusCode(401).body("codigo", equalTo(null));
        Api.tentarLogin(email, Api.ADMIN_SENHA).then().statusCode(401).contentType("application/problem+json")
                .body("codigo", equalTo("otp-necessario"));
        loginOtp(email, "000000").then().statusCode(401);
        loginOtp(email, "123456").then().statusCode(200).body("papeis", hasItems("ADMIN", "ADMINISTRATIVO"));
    }

    private io.restassured.response.Response loginOtp(String email, String otp) {
        return anonimo().contentType(ContentType.JSON)
                .body(Map.of("email", email, "senha", Api.ADMIN_SENHA, "codigoOtp", otp)).post("/auth/login");
    }

    @Test
    void cadastroERecuperacaoDisparamAcoesDoKeycloak() {
        String email = Dados.sufixo() + "@cliente.com.br";
        anonimo().contentType(ContentType.JSON).body(Fixtures.novaConta(email))
                .post("/auth/cadastro").then().statusCode(200).body("id", notNullValue());
        String token = Api.login(email, "segredo123");
        como(token).get("/auth/eu").then().statusCode(200).body("papeis", hasItems("CLIENTE"));
        anonimo().contentType(ContentType.JSON).body(Map.of("email", email)).post("/auth/recuperar-senha")
                .then().statusCode(204);
        assertEquals(java.util.List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"), KeycloakFalso.acoesEnviadas(email));
        anonimo().contentType(ContentType.JSON).body(Map.of("email", "inexistente@aeria.com.br"))
                .post("/auth/recuperar-senha").then().statusCode(204);
    }

    @Test
    void renovarRotacionaCookieESairRevogaSessao() {
        String nomeCookie = "aeria_refresh";
        String refresh = Api.tentarLogin(Api.ADMIN_EMAIL, Api.ADMIN_SENHA)
                .then().statusCode(200).extract().cookie(nomeCookie);
        String renovado = anonimo().cookie(nomeCookie, refresh).post("/auth/renovar").then().statusCode(200)
                .body("accessToken", notNullValue()).extract().cookie(nomeCookie);
        anonimo().cookie(nomeCookie, refresh).post("/auth/renovar").then().statusCode(401);
        anonimo().cookie(nomeCookie, renovado).post("/auth/sair").then().statusCode(204)
                .header("Set-Cookie", containsString("Max-Age=0"));
        anonimo().cookie(nomeCookie, renovado).post("/auth/renovar").then().statusCode(401);
        anonimo().post("/auth/renovar").then().statusCode(401);
    }
}

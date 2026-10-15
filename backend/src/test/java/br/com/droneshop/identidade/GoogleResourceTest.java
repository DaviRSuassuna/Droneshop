package br.com.droneshop.identidade;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #47 Login social com Google por ID token (RF47, D6, D37).
 * Contrato: docs/issues/047.md. O Google é o GoogleFalso.
 */
@QuarkusTest
@Tag("issue-47")
class GoogleResourceTest {

    static ValidatableResponse entrar(String idToken) {
        return anonimo().contentType(ContentType.JSON).body(Map.of("idToken", idToken))
                .when().post("/auth/google").then();
    }

    @Test
    void listaProvedores() {
        anonimo().when().get("/provedores-social").then().statusCode(200).body("nome", contains("GOOGLE"));
    }

    @Test
    void primeiroLoginCriaContaSoGoogle() {
        String email = Dados.sufixo() + "@gmail.com";
        String token = entrar(GoogleFalso.idToken("sub-" + Dados.sufixo(), email)).statusCode(200)
                .body("token", notNullValue())
                .body("perfis", contains("CLIENTE"))
                .extract().jsonPath().getString("token");
        como(token).when().get("/conta/contas-sociais").then().statusCode(200)
                .body("provedor.nome", contains("GOOGLE"));
        como(token).contentType(ContentType.JSON).body(Map.of("senhaAtual", "qualquer", "senhaNova", "outra12345"))
                .when().put("/conta/senha").then().statusCode(400).body("errors.field", hasItem("senhaAtual"));
    }

    @Test
    void mesmoSubNaoCriaSegundaConta() {
        String sub = "sub-" + Dados.sufixo();
        String email = Dados.sufixo() + "@gmail.com";
        long id1 = entrar(GoogleFalso.idToken(sub, email)).statusCode(200).extract().jsonPath().getLong("usuario.id");
        long id2 = entrar(GoogleFalso.idToken(sub, email)).statusCode(200).extract().jsonPath().getLong("usuario.id");
        org.junit.jupiter.api.Assertions.assertEquals(id1, id2);
    }

    @Test
    void vinculaAContaLocalPeloEmailVerificado() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        entrar(GoogleFalso.idToken("sub-" + Dados.sufixo(), c.email())).statusCode(200)
                .body("usuario.id", equalTo((int) c.id()));
        como(c.token()).when().get("/conta/contas-sociais").then().statusCode(200).body("$", hasSize(1));
    }

    @Test
    void emailNaoVerificadoOuTokenInvalidoResponde401() {
        entrar("sub-x|" + Dados.sufixo() + "@gmail.com|nao-verificado").statusCode(401);
        entrar("lixo").statusCode(401);
    }

    @Test
    void contaSoGoogleNaoRecebeRecuperacaoDeSenha() {
        String email = Dados.sufixo() + "@gmail.com";
        entrar(GoogleFalso.idToken("sub-" + Dados.sufixo(), email)).statusCode(200);
        anonimo().contentType(ContentType.JSON).body(Map.of("email", email))
                .when().post("/auth/recuperar-senha").then().statusCode(204);
        anonimo().queryParam("para", email).when().get("/resend-falso/emails")
                .then().statusCode(200).body("$", hasSize(0));
    }
}

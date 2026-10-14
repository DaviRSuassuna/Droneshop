package br.com.droneshop.notificacao;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
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
 * #42 Preferências de notificação, marketing e descadastro (RF18, RF41, RN10).
 * Contrato: docs/issues/042.md
 */
@QuarkusTest
@Tag("issue-42")
class PreferenciaNotificacaoResourceTest {

    static void preferir(String token, String tipo, boolean habilitado) {
        como(token).contentType(ContentType.JSON).body(List.of(Map.of("tipoNotificacao", tipo, "habilitado", habilitado)))
                .when().put("/conta/preferencias-notificacao").then().statusCode(204);
    }

    /** Carrinho do cliente com um item, parado (a rotina usa referência no futuro). */
    static void carrinhoParado(Fixtures.Cliente c) {
        String carrinho = Fixtures.novoCarrinho();
        Fixtures.adicionar(carrinho, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1);
        Api.carrinho(c.token(), carrinho).when().post("/carrinho/vincular").then().statusCode(200);
        given().queryParam("referencia", "2099-01-01T00:00:00").when().post("/admin/rotinas/carrinhos-abandonados")
                .then().statusCode(200);
    }

    static List<Map<String, Object>> emails(String para) {
        return anonimo().queryParam("para", para).when().get("/resend-falso/emails")
                .then().statusCode(200).extract().jsonPath().getList("$");
    }

    @Test
    void tudoComecaDesabilitado() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).when().get("/conta/preferencias-notificacao")
                .then().statusCode(200)
                .body("tipoNotificacao", hasItem("MARKETING"))
                .body("habilitado", everyItem(equalTo(false)));
    }

    @Test
    void tipoTransacionalNaoSeConfigura() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).contentType(ContentType.JSON)
                .body(List.of(Map.of("tipoNotificacao", "PEDIDO_CONFIRMADO", "habilitado", false)))
                .when().put("/conta/preferencias-notificacao")
                .then().statusCode(400).body("errors.field", hasItem("tipoNotificacao"));
    }

    @Test
    void semOptInNaoRecebeCarrinhoAbandonado() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        carrinhoParado(c);
        org.junit.jupiter.api.Assertions.assertTrue(emails(c.email()).isEmpty());
    }

    @Test
    void comOptInRecebeComLinkDeDescadastroQueFuncionaSemLogin() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        preferir(c.token(), "CARRINHO_ABANDONADO", true);
        carrinhoParado(c);
        List<Map<String, Object>> recebidos = emails(c.email());
        org.junit.jupiter.api.Assertions.assertEquals(1, recebidos.size());
        Matcher m = Pattern.compile("/descadastro/([A-Za-z0-9_\\-]+)").matcher(String.valueOf(recebidos.get(0).get("html")));
        org.junit.jupiter.api.Assertions.assertTrue(m.find(), "e-mail de marketing sem link de descadastro");
        anonimo().when().get("/descadastro/{token}", m.group(1))
                .then().statusCode(200).body("habilitado", equalTo(false));
        como(c.token()).when().get("/conta/preferencias-notificacao")
                .then().statusCode(200)
                .body("find { it.tipoNotificacao == 'CARRINHO_ABANDONADO' }.habilitado", equalTo(false));
    }

    @Test
    void transacionalSaiMesmoSemOptIn() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1, Fixtures.PIX);
        anonimo().queryParam("para", c.email()).when().get("/resend-falso/emails")
                .then().statusCode(200).body("$", org.hamcrest.Matchers.not(hasSize(0)));
    }

    @Test
    void descadastroComTokenInvalidoResponde404() {
        anonimo().when().get("/descadastro/{token}", "inventado").then().statusCode(404);
    }
}

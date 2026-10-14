package br.com.droneshop.pagamento;

import static br.com.droneshop.testes.Api.como;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #28 Cartões salvos por token do gateway (RF24, RN11).
 * Contrato: docs/issues/028.md
 */
@QuarkusTest
@Tag("issue-28")
class CartaoSalvoResourceTest {

    static Map<String, Object> cartao() {
        Map<String, Object> c = new HashMap<>();
        c.put("tokenGateway", "card_tok_" + System.nanoTime());
        c.put("ultimosDigitos", "4242");
        c.put("bandeira", "visa");
        c.put("validade", "12/30");
        return c;
    }

    static ValidatableResponse salvar(String token, Map<String, Object> c) {
        return como(token).contentType(ContentType.JSON).body(c).when().post("/conta/cartoes").then();
    }

    @Test
    void salvaEListaSemOToken() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        Map<String, Object> c = cartao();
        salvar(cli.token(), c).statusCode(200).body("ultimosDigitos", equalTo("4242"));
        como(cli.token()).when().get("/conta/cartoes")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].padrao", equalTo(true))
                .body(not(containsString((String) c.get("tokenGateway"))));
    }

    @Test
    void numeroCompletoNuncaEAceito() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        Map<String, Object> c = cartao();
        c.put("numero", "4242424242424242");
        salvar(cli.token(), c).statusCode(400).body("errors.field", hasItem("numero"));
    }

    @Test
    void validacoesDeDigitosEValidade() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        Map<String, Object> digitos = cartao();
        digitos.put("ultimosDigitos", "424");
        salvar(cli.token(), digitos).statusCode(400).body("errors.field", hasItem("ultimosDigitos"));
        Map<String, Object> vencido = cartao();
        vencido.put("validade", "01/20");
        salvar(cli.token(), vencido).statusCode(400).body("errors.field", hasItem("validade"));
    }

    @Test
    void trocaOPadraoEExclui() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        salvar(cli.token(), cartao()).statusCode(200);
        long segundo = salvar(cli.token(), cartao()).statusCode(200).extract().jsonPath().getLong("id");
        como(cli.token()).when().patch("/conta/cartoes/{id}/padrao", segundo).then().statusCode(204);
        como(cli.token()).when().get("/conta/cartoes").then().statusCode(200)
                .body("findAll { it.padrao }", hasSize(1))
                .body("find { it.padrao }.id", equalTo((int) segundo));
        como(cli.token()).when().delete("/conta/cartoes/{id}", segundo).then().statusCode(204);
        como(cli.token()).when().get("/conta/cartoes").then().statusCode(200).body("$", hasSize(1));
    }

    @Test
    void cartaoDeOutroClienteResponde404() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        long id = salvar(dono.token(), cartao()).statusCode(200).extract().jsonPath().getLong("id");
        como(outro.token()).when().delete("/conta/cartoes/{id}", id).then().statusCode(404);
    }

    @Test
    void pagaComCartaoSalvo() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        Map<String, Object> c = cartao();
        c.put("tokenGateway", MercadoPagoFalso.CARTAO_APROVADO);
        long idCartao = salvar(cli.token(), c).statusCode(200).extract().jsonPath().getLong("id");
        long pedido = Fixtures.pedidoPendente(cli, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1, Fixtures.CARTAO);
        como(cli.token()).contentType(ContentType.JSON)
                .body(Map.of("idMetodo", Fixtures.CARTAO, "idCartaoSalvo", idCartao))
                .when().post("/pedidos/{id}/pagamentos", pedido)
                .then().statusCode(200).body("status.nome", equalTo("APROVADO"));
    }
}

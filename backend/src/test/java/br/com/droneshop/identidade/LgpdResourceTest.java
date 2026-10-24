package br.com.droneshop.identidade;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

/**
 * #46 Consentimento LGPD e exclusão/anonimização de conta (RF51, RNF03).
 * Contrato: docs/issues/046.md
 */
@QuarkusTest
@Tag("issue-46")
class LgpdResourceTest {

    @Test
    void termosVigentes() {
        anonimo().when().get("/loja/termos").then().statusCode(200)
                .body("tipo", hasItems("TERMOS_USO", "POLITICA_PRIVACIDADE"));
    }

    @Test
    void contaSemConsentimentoObrigatorioResponde400() {
        Map<String, Object> c = Fixtures.novaConta(Dados.sufixo() + "@cliente.com.br");
        c.put("consentimentos", List.of(Map.of("tipo", "TERMOS_USO", "versao", "2026-10")));
        anonimo().contentType(ContentType.JSON).body(c).when().post("/clientes")
                .then().statusCode(400).body("errors.field", hasItem("consentimentos"));
        c.put("consentimentos", List.of(Map.of("tipo", "TERMOS_USO", "versao", "2000-01"),
                Map.of("tipo", "POLITICA_PRIVACIDADE", "versao", "2026-10")));
        anonimo().contentType(ContentType.JSON).body(c).when().post("/clientes")
                .then().statusCode(400).body("errors.field", hasItem("consentimentos"));
    }

    @Test
    void consentimentosDatadosEVersionados() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).contentType(ContentType.JSON).body(Map.of("tipo", "MARKETING", "versao", "2026-10"))
                .when().post("/conta/consentimentos").then().statusCode(204);
        como(c.token()).when().get("/conta/consentimentos").then().statusCode(200)
                .body("$", hasSize(3))
                .body("tipo", hasItem("MARKETING"))
                .body("[0].dataConsentimento", notNullValue());
    }

    @Test
    void exclusaoAnonimizaEPreservaOsPedidos() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(150.0, 5).id(), 1, Fixtures.PIX);
        JsonPath antes = given().when().get("/pedidos/{id}", pedido).then().statusCode(200).extract().jsonPath();

        como(c.token()).contentType(ContentType.JSON).body(Map.of("senha", c.senha()))
                .when().post("/conta/exclusao").then().statusCode(204);

        Api.tentarLogin(c.email(), c.senha()).then().statusCode(401);
        given().when().get("/pedidos/{id}", pedido).then().statusCode(200)
                .body("cliente.nome", equalTo("Cliente removido"))
                .body("valorTotal", equalTo(antes.getFloat("valorTotal")))
                .body("itens.size()", equalTo(antes.getList("itens").size()))
                .body("enderecoEntrega.logradouro", equalTo(antes.getString("enderecoEntrega.logradouro")));
    }

    @Test
    void exclusaoExigeASenha() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).contentType(ContentType.JSON).body(Map.of("senha", "errada"))
                .when().post("/conta/exclusao").then().statusCode(400).body("errors.field", hasItem("senha"));
    }
}

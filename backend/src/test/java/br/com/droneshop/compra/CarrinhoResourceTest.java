package br.com.droneshop.compra;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.carrinho;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
 * #24 Carrinho anônimo por token e vínculo no login (RF16, RF17, RF18, RN17).
 * Contrato: docs/issues/024.md
 */
@QuarkusTest
@Tag("issue-24")
class CarrinhoResourceTest {

    static ValidatableResponse adicionar(String token, long idVariante, int quantidade) {
        return carrinho(token).contentType(ContentType.JSON)
                .body(Map.of("idVariante", idVariante, "quantidade", quantidade))
                .when().post("/carrinho/itens").then();
    }

    @Test
    void listaOsStatusDeCarrinho() {
        anonimo().when().get("/status-carrinho").then().statusCode(200)
                .body("nome", contains("ATIVO", "ABANDONADO", "CONVERTIDO", "EXPIRADO"));
    }

    @Test
    void criaCarrinhoSemLogin() {
        anonimo().when().post("/carrinho")
                .then().statusCode(200)
                .body("token", notNullValue())
                .body("status.nome", equalTo("ATIVO"))
                .body("itens", hasSize(0))
                .body("total", equalTo(0.0f));
        carrinho("token-que-nao-existe").when().get("/carrinho").then().statusCode(404);
    }

    @Test
    void mesmaVarianteSomaAQuantidadeEOTotalBate() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 10);
        String c = Fixtures.novoCarrinho();
        adicionar(c, p.idVariante(), 1).statusCode(200);
        adicionar(c, p.idVariante(), 2).statusCode(200)
                .body("itens", hasSize(1))
                .body("itens[0].quantidade", equalTo(3))
                .body("itens[0].precoUnitario", equalTo(1000.0f))
                .body("subtotal", equalTo(3000.0f))
                .body("total", equalTo(3000.0f));
    }

    @Test
    void alteraERemoveItem() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 10);
        String c = Fixtures.novoCarrinho();
        long idItem = adicionar(c, p.idVariante(), 1).statusCode(200).extract().jsonPath().getLong("itens[0].id");
        carrinho(c).contentType(ContentType.JSON).body(Map.of("quantidade", 4))
                .when().patch("/carrinho/itens/{id}", idItem)
                .then().statusCode(200).body("itens[0].quantidade", equalTo(4));
        carrinho(c).when().delete("/carrinho/itens/{id}", idItem)
                .then().statusCode(200).body("itens", hasSize(0));
    }

    @Test
    void validacoesDoItem() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 2);
        String c = Fixtures.novoCarrinho();
        adicionar(c, p.idVariante(), 0).statusCode(400).body("errors.field", hasItem("quantidade"));
        adicionar(c, p.idVariante(), 3).statusCode(409);
    }

    @Test
    void produtoEmRascunhoNaoEntra() {
        String s = Dados.sufixo();
        Map<String, Object> d = Fixtures.drone("Rascunho " + s, Fixtures.arvore(),
                Fixtures.lista(Fixtures.variante("RS-" + s.toUpperCase(), 100.0, 5)));
        d.put("idStatus", Fixtures.PRODUTO_RASCUNHO);
        long idVariante = given().contentType(ContentType.JSON).body(d).when().post("/drones")
                .then().statusCode(200).extract().jsonPath().getLong("variantes[0].id");
        adicionar(Fixtures.novoCarrinho(), idVariante, 1).statusCode(400).body("errors.field", hasItem("idVariante"));
    }

    @Test
    void vincularJuntaOAnonimoAoDoCliente() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        Fixtures.ProdutoCriado a = Fixtures.criarDrone(1000.0, 10);
        Fixtures.ProdutoCriado b = Fixtures.criarAcessorio(100.0, 10);

        String doCliente = Fixtures.novoCarrinho();
        adicionar(doCliente, a.idVariante(), 1).statusCode(200);
        carrinho(cli.token(), doCliente).when().post("/carrinho/vincular").then().statusCode(200);

        String anonimo = Fixtures.novoCarrinho();
        adicionar(anonimo, a.idVariante(), 2).statusCode(200);
        adicionar(anonimo, b.idVariante(), 1).statusCode(200);
        carrinho(cli.token(), anonimo).when().post("/carrinho/vincular")
                .then().statusCode(200)
                .body("itens", hasSize(2))
                .body("itens.find { it.variante.id == " + a.idVariante() + " }.quantidade", equalTo(3));

        carrinho(anonimo).when().get("/carrinho").then().statusCode(200).body("status.nome", equalTo("EXPIRADO"));
        como(cli.token()).when().get("/conta/carrinho").then().statusCode(200).body("itens", hasSize(2));
    }

    @Test
    void loginComOHeaderVinculaSozinho() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 10);
        String c = Fixtures.novoCarrinho();
        adicionar(c, p.idVariante(), 1).statusCode(200);
        String token = carrinho(c).contentType(ContentType.JSON)
                .body(Map.of("email", cli.email(), "senha", cli.senha()))
                .when().post("/auth/login").then().statusCode(200).extract().jsonPath().getString("token");
        como(token).when().get("/conta/carrinho").then().statusCode(200)
                .body("itens.variante.id", hasItem((int) p.idVariante()));
    }

    @Test
    void rotinaMarcaAbandonado() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 10);
        String c = Fixtures.novoCarrinho();
        adicionar(c, p.idVariante(), 1).statusCode(200);
        given().queryParam("referencia", "2099-01-01T00:00:00")
                .when().post("/admin/rotinas/carrinhos-abandonados")
                .then().statusCode(200).body("processados", greaterThanOrEqualTo(1));
        carrinho(c).when().get("/carrinho").then().statusCode(200).body("status.nome", equalTo("ABANDONADO"));
    }
}

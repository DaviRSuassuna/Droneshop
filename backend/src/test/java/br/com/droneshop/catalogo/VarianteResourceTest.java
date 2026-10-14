package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

/**
 * #9 VarianteProduto: SKU, preço, custo, estoque e embalagem (D13, D16, D26, RN18).
 * Contrato: docs/issues/009.md
 */
@QuarkusTest
@Tag("issue-9")
class VarianteResourceTest {

    static String sku() {
        return "SKU-" + Dados.sufixo().toUpperCase();
    }

    static io.restassured.response.ValidatableResponse postDrone(List<Map<String, Object>> variantes) {
        return given().contentType(ContentType.JSON)
                .body(Fixtures.drone("Drone " + Dados.sufixo(), Fixtures.arvore(), variantes))
                .when().post("/drones").then();
    }

    @Test
    void criaVarianteComMargemCalculada() {
        Map<String, Object> v = Fixtures.variante(sku(), 1000.0, 10);
        v.put("custoAquisicao", 600.0);
        postDrone(Fixtures.lista(v)).statusCode(200)
                .body("variantes", hasSize(1))
                .body("variantes[0].id", notNullValue())
                .body("variantes[0].preco", equalTo(1000.0f))
                .body("variantes[0].margemPercentual", equalTo(40.0f))
                .body("variantes[0].estoqueFisico", equalTo(10))
                .body("variantes[0].ehKit", equalTo(false))
                .body("preco", nullValue());
    }

    @Test
    void skuRepetidoEmQualquerProdutoResponde400() {
        String sku = sku();
        postDrone(Fixtures.lista(Fixtures.variante(sku, 100.0, 1))).statusCode(200);
        postDrone(Fixtures.lista(Fixtures.variante(sku, 100.0, 1))).statusCode(400)
                .body("errors.field", hasItem("variantes[0].sku"));
    }

    @Test
    void produtoAtivoSemVarianteResponde400() {
        postDrone(Fixtures.lista()).statusCode(400).body("errors.field", hasItem("variantes"));
    }

    @Test
    void varianteSemEmbalagemNaoPublica() {
        Map<String, Object> v = Fixtures.variante(sku(), 100.0, 1);
        v.remove("pesoEmbalagemKg");
        postDrone(Fixtures.lista(v)).statusCode(400)
                .body("errors.field", hasItem("variantes[0].pesoEmbalagemKg"));
    }

    @Test
    void precoZeroResponde400() {
        postDrone(Fixtures.lista(Fixtures.variante(sku(), 0.0, 1))).statusCode(400)
                .body("errors.field", hasItem("variantes[0].preco"));
    }

    @Test
    void rascunhoAceitaVarianteIncompleta() {
        Map<String, Object> d = Fixtures.drone("Rascunho " + Dados.sufixo(), Fixtures.arvore(),
                Fixtures.lista(new HashMap<>(Map.of("nome", "Padrão", "sku", sku()))));
        d.put("idStatus", Fixtures.PRODUTO_RASCUNHO);
        given().contentType(ContentType.JSON).body(d).when().post("/drones").then().statusCode(200);
    }

    @Test
    void buscaVariantePorSku() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1500.0, 3);
        given().queryParam("busca", p.sku()).when().get("/variantes")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) p.idVariante()))
                .body("items[0].produto.id", equalTo((int) p.id()));
        given().when().get("/variantes/{id}", Dados.ID_INEXISTENTE).then().statusCode(404);
    }

    @Test
    void putDaVarianteNaoMudaOEstoque() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1500.0, 3);
        Map<String, Object> v = Fixtures.variante(p.sku(), 1800.0, 999);
        given().contentType(ContentType.JSON).body(v).when().put("/variantes/{id}", p.idVariante())
                .then().statusCode(204);
        given().when().get("/variantes/{id}", p.idVariante())
                .then().statusCode(200)
                .body("preco", equalTo(1800.0f))
                .body("estoqueFisico", equalTo(3));
    }

    @Test
    void putDoProdutoAlteraCriaERemoveVariantes() {
        Fixtures.Arvore a = Fixtures.arvore();
        String sku1 = sku(), sku2 = sku(), sku3 = sku();
        Map<String, Object> d = Fixtures.drone("Varias " + Dados.sufixo(), a, Fixtures.lista(
                Fixtures.variante(sku1, 100.0, 1), Fixtures.variante(sku2, 200.0, 1)));
        JsonPath criado = given().contentType(ContentType.JSON).body(d).when().post("/drones")
                .then().statusCode(200).extract().jsonPath();
        long id = criado.getLong("id");
        long idV1 = criado.getLong("variantes.find { it.sku == '" + sku1 + "' }.id");

        Map<String, Object> v1 = Fixtures.variante(sku1, 150.0, 1);
        v1.put("id", idV1);
        d.put("variantes", Fixtures.lista(v1, Fixtures.variante(sku3, 300.0, 2)));
        given().contentType(ContentType.JSON).body(d).when().put("/drones/{id}", id).then().statusCode(204);

        given().when().get("/produtos/{id}", id)
                .then().statusCode(200)
                .body("variantes", hasSize(2))
                .body("variantes.sku", hasItems(sku1, sku3))
                .body("variantes.find { it.sku == '" + sku1 + "' }.preco", equalTo(150.0f));
    }
}

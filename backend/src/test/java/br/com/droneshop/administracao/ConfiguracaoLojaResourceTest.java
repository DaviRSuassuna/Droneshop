package br.com.droneshop.administracao;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #6 ConfiguracaoLoja: registro único de parâmetros da loja.
 * Contrato: docs/issues/006.md. O teste que altera devolve os valores anteriores no fim.
 */
@QuarkusTest
@Tag("issue-6")
class ConfiguracaoLojaResourceTest {

    /** Configuração atual, sem o id, pronta para um PUT. */
    static Map<String, Object> atual() {
        Map<String, Object> c = new HashMap<>(given().when().get("/admin/configuracao")
                .then().statusCode(200).extract().jsonPath().getMap("$"));
        c.remove("id");
        return c;
    }

    static void gravar(Map<String, Object> c) {
        given().contentType(ContentType.JSON).body(c)
                .when().put("/admin/configuracao").then().statusCode(204);
    }

    @Test
    void cargaTrazOsValoresDoD8() {
        given().when().get("/admin/configuracao")
                .then().statusCode(200)
                .body("id", notNullValue())
                .body("prazoArrependimentoDias", equalTo(7))
                .body("prazoAnaliseDevolucaoDiasUteis", equalTo(5))
                .body("prazoGarantiaDias", equalTo(365))
                .body("freteReversoPorContaLoja", equalTo(true));
    }

    @Test
    void alteraERele() {
        Map<String, Object> original = atual();
        Map<String, Object> c = new HashMap<>(original);
        c.put("prazoArrependimentoDias", 10);
        c.put("remetentePadrao", "Aeria Palmas");
        given().contentType(ContentType.JSON).body(c)
                .when().put("/admin/configuracao").then().statusCode(204);
        given().when().get("/admin/configuracao")
                .then().statusCode(200)
                .body("prazoArrependimentoDias", equalTo(10))
                .body("remetentePadrao", equalTo("Aeria Palmas"));
        gravar(original);
    }

    @Test
    void valoresInvalidosRespondem400() {
        Map<String, Object> c = atual();
        c.put("cepOrigemFrete", "7700");
        c.put("prazoArrependimentoDias", 6);
        c.put("prazoGarantiaDias", 0);
        given().contentType(ContentType.JSON).body(c)
                .when().put("/admin/configuracao")
                .then().statusCode(400)
                .body("errors.field", hasItem("cepOrigemFrete"))
                .body("errors.field", hasItem("prazoArrependimentoDias"))
                .body("errors.field", hasItem("prazoGarantiaDias"));
    }

    @Test
    void naoExistePostNemDelete() {
        given().contentType(ContentType.JSON).body(atual())
                .when().post("/admin/configuracao").then().statusCode(405);
        given().when().delete("/admin/configuracao").then().statusCode(405);
    }

    @Test
    void lojaVeSoOQueMostra() {
        anonimo().when().get("/loja/configuracao")
                .then().statusCode(200)
                .body("prazoArrependimentoDias", equalTo(7))
                .body("dadosFiscais", org.hamcrest.Matchers.nullValue());
    }
}

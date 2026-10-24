package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #8 Acessório como especialização de Produto (D27).
 * Contrato: docs/issues/008.md
 */
@QuarkusTest
@Tag("issue-8")
class AcessorioResourceTest {

    static Map<String, Object> acessorio(String nome) {
        return Fixtures.acessorio(nome, Fixtures.arvore(), Fixtures.dadosComerciais("AC-" + Dados.sufixo().toUpperCase(), 199.0, 5));
    }

    @Test
    void criaAcessorioComTipo() {
        given().contentType(ContentType.JSON).body(acessorio("Bateria " + Dados.sufixo()))
                .when().post("/acessorios")
                .then().statusCode(200)
                .body("tipo.nome", equalTo("ACESSORIO"))
                .body("tipoAcessorio", equalTo("Bateria"))
                .body("autonomiaMinutos", nullValue());
    }

    @Test
    void ativoSemTipoResponde400() {
        Map<String, Object> a = acessorio("Sem tipo " + Dados.sufixo());
        a.put("tipoAcessorio", "");
        given().contentType(ContentType.JSON).body(a).when().post("/acessorios")
                .then().statusCode(400).body("errors.field", hasItem("tipoAcessorio"));
    }

    @Test
    void alteraAcessorio() {
        String s = Dados.sufixo();
        Map<String, Object> a = acessorio("Helice " + s);
        long id = Fixtures.criar("/acessorios", a);
        a.put("tipoAcessorio", "Hélice");
        given().contentType(ContentType.JSON).body(a).when().put("/acessorios/{id}", id)
                .then().statusCode(204);
        given().when().get("/produtos/{id}", id)
                .then().statusCode(200)
                .body("tipo.nome", equalTo("ACESSORIO"))
                .body("tipoAcessorio", equalTo("Hélice"));
    }

    @Test
    void putEmIdDeDroneResponde404() {
        long idDrone = Fixtures.criarDrone(5000.0, 1).id();
        given().contentType(ContentType.JSON).body(acessorio("Errado " + Dados.sufixo()))
                .when().put("/acessorios/{id}", idDrone)
                .then().statusCode(404);
    }
}

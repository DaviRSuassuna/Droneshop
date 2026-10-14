package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #8 Drone como especialização de Produto (D14, D27).
 * Contrato: docs/issues/008.md
 */
@QuarkusTest
@Tag("issue-8")
class DroneResourceTest {

    static Map<String, Object> drone(String nome) {
        Fixtures.Arvore a = Fixtures.arvore();
        return Fixtures.drone(nome, a, Fixtures.lista(
                Fixtures.variante("DR-" + Dados.sufixo().toUpperCase(), 7999.0, 5)));
    }

    static long criar(Map<String, Object> d) {
        return given().contentType(ContentType.JSON).body(d).when().post("/drones")
                .then().statusCode(200).extract().jsonPath().getLong("id");
    }

    @Test
    void listaOsTiposDeProduto() {
        given().when().get("/tipos-produto")
                .then().statusCode(200)
                .body("nome", hasItems("DRONE", "ACESSORIO"));
    }

    @Test
    void criaDroneComTodasAsSpecs() {
        String s = Dados.sufixo();
        given().contentType(ContentType.JSON).body(drone("Falcon " + s))
                .when().post("/drones")
                .then().statusCode(200)
                .body("id", notNullValue())
                .body("tipo.nome", equalTo("DRONE"))
                .body("slug", equalTo("falcon-" + s))
                .body("marca.nome", notNullValue())
                .body("subcategoria.categoria.nome", notNullValue())
                .body("fornecedor.razaoSocial", notNullValue())
                .body("pesoGramas", equalTo(895))
                .body("autonomiaMinutos", equalTo(46))
                .body("gpsIntegrado", equalTo(true))
                .body("classeAnac", equalTo("Classe 3"));
    }

    @Test
    void specNegativaResponde400() {
        Map<String, Object> d = drone("Negativo " + Dados.sufixo());
        d.put("autonomiaMinutos", -1);
        d.put("alcanceMetros", -10);
        given().contentType(ContentType.JSON).body(d).when().post("/drones")
                .then().statusCode(400)
                .body("errors.field", hasItems("autonomiaMinutos", "alcanceMetros"));
    }

    @Test
    void ativoSemPesoResponde400() {
        Map<String, Object> d = drone("Sem peso " + Dados.sufixo());
        d.remove("pesoGramas");
        given().contentType(ContentType.JSON).body(d).when().post("/drones")
                .then().statusCode(400).body("errors.field", hasItem("pesoGramas"));
    }

    @Test
    void exigeAnacSemClasseResponde400() {
        Map<String, Object> d = drone("Anac " + Dados.sufixo());
        d.put("classeAnac", "");
        given().contentType(ContentType.JSON).body(d).when().post("/drones")
                .then().statusCode(400).body("errors.field", hasItem("classeAnac"));
    }

    @Test
    void rascunhoSoComStatusSalva() {
        given().contentType(ContentType.JSON).body(Map.of("idStatus", Fixtures.PRODUTO_RASCUNHO))
                .when().post("/drones")
                .then().statusCode(200).body("tipo.nome", equalTo("DRONE"));
    }

    @Test
    void alteraDroneEConsultaPorProdutos() {
        String s = Dados.sufixo();
        Map<String, Object> d = drone("Antes " + s);
        long id = criar(d);
        d.put("nome", "Depois " + s);
        d.put("autonomiaMinutos", 50);
        given().contentType(ContentType.JSON).body(d).when().put("/drones/{id}", id)
                .then().statusCode(204);
        given().when().get("/produtos/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Depois " + s))
                .body("tipo.nome", equalTo("DRONE"))
                .body("autonomiaMinutos", equalTo(50));
    }

    @Test
    void putEmDroneInexistenteOuEmAcessorioResponde404() {
        Map<String, Object> d = drone("Fantasma " + Dados.sufixo());
        given().contentType(ContentType.JSON).body(d).when().put("/drones/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        long idAcessorio = Fixtures.criarAcessorio(99.0, 1).id();
        given().contentType(ContentType.JSON).body(d).when().put("/drones/{id}", idAcessorio)
                .then().statusCode(404);
    }

    @Test
    void produtosFiltraPorTipo() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.criarDrone(a, 5000.0, 1);
        Fixtures.criarAcessorio(a, 100.0, 1);
        given().queryParam("idMarca", a.idMarca()).queryParam("idTipo", Fixtures.TIPO_DRONE)
                .when().get("/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items.tipo.nome", everyItem(equalTo("DRONE")));
    }

    @Test
    void postEPutGenericosEmProdutosDeixamDeExistir() {
        given().contentType(ContentType.JSON).body(drone("Generico " + Dados.sufixo()))
                .when().post("/produtos").then().statusCode(405);
    }

    @Test
    void excluiDroneSemPedido() {
        long id = criar(drone("Excluir " + Dados.sufixo()));
        given().when().delete("/produtos/{id}", id).then().statusCode(204);
        given().when().get("/produtos/{id}", id).then().statusCode(404);
    }
}

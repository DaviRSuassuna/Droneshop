package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
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

/**
 * #10 Kits e combos: variante composta por ComponenteKit (D7, D16).
 * Contrato: docs/issues/010.md
 */
@QuarkusTest
@Tag("issue-10")
class KitResourceTest {

    static Map<String, Object> kit(List<Map<String, Object>> componentes) {
        Map<String, Object> k = new HashMap<>();
        k.put("nome", "Fly More");
        k.put("sku", "KIT-" + Dados.sufixo().toUpperCase());
        k.put("preco", 9999.0);
        k.put("custoAquisicao", 6000.0);
        k.put("ehKit", true);
        k.put("ativa", true);
        k.put("componentes", componentes);
        return k;
    }

    static Map<String, Object> componente(long idVariante, int quantidade) {
        return Map.of("idVariante", idVariante, "quantidade", quantidade);
    }

    static io.restassured.response.ValidatableResponse postKit(Map<String, Object> kit) {
        return given().contentType(ContentType.JSON)
                .body(Fixtures.drone("Kit " + Dados.sufixo(), Fixtures.arvore(), Fixtures.lista(kit)))
                .when().post("/drones").then();
    }

    @Test
    void kitDerivaDisponibilidadeEPeso() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).idVariante();
        long idBateria = Fixtures.criarAcessorio(499.0, 7).idVariante();
        postKit(kit(List.of(componente(idDrone, 1), componente(idBateria, 2)))).statusCode(200)
                .body("variantes[0].ehKit", equalTo(true))
                .body("variantes[0].estoqueFisico", nullValue())
                .body("variantes[0].disponivel", equalTo(3)) // min(10/1, 7/2)
                .body("variantes[0].componentes", hasSize(2))
                .body("variantes[0].pesoEmbalagemKg", equalTo(4.5f)) // 1,5 + 2 x 1,5
                .body("variantes[0].pesoSobrescrito", equalTo(false));
    }

    @Test
    void pesoInformadoSobrescreveODerivado() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).idVariante();
        Map<String, Object> k = kit(List.of(componente(idDrone, 1)));
        k.put("pesoEmbalagemKg", 3.0);
        postKit(k).statusCode(200)
                .body("variantes[0].pesoEmbalagemKg", equalTo(3.0f))
                .body("variantes[0].pesoSobrescrito", equalTo(true));
    }

    @Test
    void kitComEstoqueResponde400() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).idVariante();
        Map<String, Object> k = kit(List.of(componente(idDrone, 1)));
        k.put("estoqueFisico", 5);
        postKit(k).statusCode(400).body("errors.field", hasItem("variantes[0].estoqueFisico"));
    }

    @Test
    void kitSemComponentesResponde400() {
        postKit(kit(List.of())).statusCode(400).body("errors.field", hasItem("variantes[0].componentes"));
    }

    @Test
    void kitDentroDeKitResponde400() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).idVariante();
        long idKit = postKit(kit(List.of(componente(idDrone, 1)))).statusCode(200)
                .extract().jsonPath().getLong("variantes[0].id");
        postKit(kit(List.of(componente(idKit, 1)))).statusCode(400)
                .body("errors.field", hasItem("variantes[0].componentes"));
    }

    @Test
    void quantidadeZeroResponde400() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).idVariante();
        postKit(kit(List.of(componente(idDrone, 0)))).statusCode(400)
                .body("errors.field", hasItem("variantes[0].componentes[0].quantidade"));
    }

    @Test
    void varianteComumTambemTrazDisponivel() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 4);
        given().when().get("/variantes/{id}", p.idVariante())
                .then().statusCode(200).body("disponivel", equalTo(4));
    }
}

package br.com.droneshop.administracao;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #18 Dashboard do admin com indicadores calculados no backend.
 * Contrato: docs/issues/018.md
 */
@QuarkusTest
@Tag("issue-18")
class DashboardResourceTest {

    @Test
    void semDatasUsaOsUltimos30Dias() {
        given().when().get("/admin/dashboard")
                .then().statusCode(200)
                .body("periodo.dataFim", equalTo(LocalDate.now().toString()))
                .body("periodo.dataInicio", equalTo(LocalDate.now().minusDays(30).toString()))
                .body("pedidosPorStatus", hasSize(8))
                .body("faturamento", notNullValue())
                .body("ultimosPedidos.size()", lessThanOrEqualTo(5));
    }

    @Test
    void faturamentoContaPedidosPagosEmDiante() {
        given().queryParam("dataInicio", "2000-01-01").queryParam("dataFim", LocalDate.now().toString())
                .when().get("/admin/dashboard")
                .then().statusCode(200)
                .body("faturamento", greaterThan(0f))
                .body("quantidadePedidos", greaterThan(0));
    }

    @Test
    void varianteAbaixoDoMinimoApareceNoEstoqueBaixo() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 5);
        given().contentType(ContentType.JSON)
                .body(Map.of("idVariante", p.idVariante(), "idTipo", Fixtures.MOV_AJUSTE, "quantidade", 0))
                .when().post("/estoque/movimentos").then().statusCode(200);
        given().when().get("/admin/dashboard")
                .then().statusCode(200)
                .body("estoqueBaixo.variante.id", hasItem((int) p.idVariante()));
    }

    @Test
    void periodoInvertidoResponde400() {
        given().queryParam("dataInicio", "2026-10-31").queryParam("dataFim", "2026-10-01")
                .when().get("/admin/dashboard")
                .then().statusCode(400).body("errors.field", hasItem("dataFim"));
    }
}

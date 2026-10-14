package br.com.droneshop.pedido;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #17 Transições de status do pedido com histórico (RF26, RN08, D11, D30).
 * Contrato: docs/issues/017.md. Cada teste muda no máximo um pedido da carga
 * de cada status (a carga tem três por status).
 */
@QuarkusTest
@Tag("issue-17")
class StatusPedidoResourceTest {

    @Test
    void transicaoValidaGravaHistoricoManualComAutor() {
        long id = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_PENDENTE);
        given().contentType(ContentType.JSON).body(Map.of("idStatus", Fixtures.PEDIDO_PAGO, "observacao", "Pago no balcão"))
                .when().patch("/pedidos/{id}/status", id)
                .then().statusCode(204);
        given().when().get("/pedidos/{id}", id).then().statusCode(200).body("status.nome", equalTo("PAGO"));
        given().when().get("/pedidos/{id}/historico", id)
                .then().statusCode(200)
                .body("[-1].statusAnterior.nome", equalTo("PENDENTE"))
                .body("[-1].statusNovo.nome", equalTo("PAGO"))
                .body("[-1].origem.nome", equalTo("MANUAL"))
                .body("[-1].autor.id", notNullValue())
                .body("[-1].observacao", equalTo("Pago no balcão"));
    }

    @Test
    void transicaoInvalidaResponde409SemMudarNada() {
        long id = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_ENTREGUE);
        Fixtures.mudarStatus(id, Fixtures.PEDIDO_PAGO)
                .then().statusCode(409)
                .contentType("application/problem+json")
                .body("type", endsWith("/errors/conflict"));
        given().when().get("/pedidos/{id}", id).then().statusCode(200).body("status.nome", equalTo("ENTREGUE"));
    }

    @Test
    void canceladoNaoVoltaANenhumStatus() {
        long id = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_CANCELADO);
        for (long status : new long[] { Fixtures.PEDIDO_PENDENTE, Fixtures.PEDIDO_PAGO, Fixtures.PEDIDO_ENVIADO })
            Fixtures.mudarStatus(id, status).then().statusCode(409);
    }

    @Test
    void historicoEmOrdemCronologica() {
        long id = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_SEPARACAO);
        Fixtures.mudarStatus(id, Fixtures.PEDIDO_ENVIADO).then().statusCode(204);
        Fixtures.mudarStatus(id, Fixtures.PEDIDO_ENTREGUE).then().statusCode(204);
        var novos = given().when().get("/pedidos/{id}/historico", id)
                .then().statusCode(200).extract().jsonPath().getList("statusNovo.nome", String.class);
        org.junit.jupiter.api.Assertions.assertEquals(List.of("ENVIADO", "ENTREGUE"),
                novos.subList(novos.size() - 2, novos.size()));
    }

    @Test
    void statusInexistenteResponde400EPedidoInexistente404() {
        long id = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_PAGO);
        Fixtures.mudarStatus(id, 99).then().statusCode(400).body("errors.field", hasItem("idStatus"));
        Fixtures.mudarStatus(Dados.ID_INEXISTENTE, Fixtures.PEDIDO_PAGO).then().statusCode(404);
    }
}

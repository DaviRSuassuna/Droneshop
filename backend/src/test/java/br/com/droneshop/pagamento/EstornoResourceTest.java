package br.com.droneshop.pagamento;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;

/**
 * #36 Estorno total ou parcial de pagamento (RF25).
 * Contrato: docs/issues/036.md
 */
@QuarkusTest
@Tag("issue-36")
class EstornoResourceTest {

    /** Pagamento aprovado de um pedido novo: [id, valor]. */
    static Object[] pagamentoAprovado() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(200.0, 5).idVariante(), 1);
        JsonPath j = given().when().get("/pedidos/{id}/pagamentos", pedido).then().statusCode(200).extract().jsonPath();
        return new Object[] { j.getLong("find { it.vigente }.id"), new BigDecimal(j.getString("find { it.vigente }.valor")) };
    }

    static ValidatableResponse estornar(long idPagamento, BigDecimal valor) {
        return given().contentType(ContentType.JSON).body(Map.of("valor", valor, "motivo", "Teste"))
                .when().post("/pagamentos/{id}/estorno", idPagamento).then();
    }

    @Test
    void estornoParcialDepoisTotal() {
        Object[] p = pagamentoAprovado();
        long id = (Long) p[0];
        BigDecimal valor = (BigDecimal) p[1];
        estornar(id, new BigDecimal("50.00")).statusCode(200).body("pagamento.status.nome", equalTo("ESTORNADO_PARCIAL"));
        estornar(id, valor.subtract(new BigDecimal("50.00"))).statusCode(200).body("pagamento.status.nome", equalTo("ESTORNADO"));
        given().when().get("/pagamentos/{id}/estornos", id).then().statusCode(200).body("$", hasSize(2));
    }

    @Test
    void somaNaoPassaDoValorPago() {
        Object[] p = pagamentoAprovado();
        estornar((Long) p[0], ((BigDecimal) p[1]).add(BigDecimal.ONE)).statusCode(400).body("errors.field", hasItem("valor"));
        estornar((Long) p[0], BigDecimal.ZERO).statusCode(400).body("errors.field", hasItem("valor"));
    }

    @Test
    void pagamentoTotalmenteEstornadoNaoAceitaOutro() {
        Object[] p = pagamentoAprovado();
        estornar((Long) p[0], (BigDecimal) p[1]).statusCode(200);
        estornar((Long) p[0], BigDecimal.ONE).statusCode(409);
    }
}

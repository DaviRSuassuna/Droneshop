package br.com.droneshop.estoque;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
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
import io.restassured.response.ValidatableResponse;

/**
 * #34 Unidade individual por número de série na expedição (RF15, RN12, RN14, D10).
 * Contrato: docs/issues/034.md
 */
@QuarkusTest
@Tag("issue-34")
class UnidadeIndividualResourceTest {

    static ValidatableResponse unidade(long idProduto, String serie) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("idProduto", idProduto, "numeroSerie", serie, "registroAnac", ""))
                .when().post("/unidades").then();
    }

    static long novaUnidade(long idProduto) {
        return unidade(idProduto, "SN-" + Dados.sufixo().toUpperCase()).statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    static ValidatableResponse vincular(long pedido, long item, List<Long> unidades) {
        return given().contentType(ContentType.JSON).body(Map.of("idsUnidades", unidades))
                .when().post("/pedidos/{id}/itens/{idItem}/unidades", pedido, item).then();
    }

    /** Pedido PAGO de 2 unidades de um drone, já em SEPARACAO. Devolve [pedido, item, produto]. */
    static long[] pedidoDeDroneEmSeparacao() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long v = Fixtures.criarDrone(3000.0, 5).id();
        long pedido = Fixtures.pedidoPago(c, v, 2);
        Fixtures.mudarStatus(pedido, Fixtures.PEDIDO_SEPARACAO).then().statusCode(204);
        long item = given().when().get("/pedidos/{id}", pedido).then().statusCode(200)
                .extract().jsonPath().getLong("itens[0].id");
        return new long[] { pedido, item, v };
    }

    @Test
    void listaStatusDaUnidade() {
        given().when().get("/status-unidade").then().statusCode(200)
                .body("nome", contains("DISPONIVEL", "RESERVADA", "VENDIDA", "DEVOLVIDA"));
    }

    @Test
    void numeroDeSerieUnicoESoParaDrone() {
        long v = Fixtures.criarDrone(3000.0, 1).id();
        String serie = "SN-" + Dados.sufixo().toUpperCase();
        unidade(v, serie).statusCode(200).body("status.nome", equalTo("DISPONIVEL"));
        unidade(v, serie).statusCode(400).body("errors.field", hasItem("numeroSerie"));
        long acessorio = Fixtures.criarAcessorio(100.0, 1).id();
        unidade(acessorio, "SN-" + Dados.sufixo()).statusCode(400).body("errors.field", hasItem("idProduto"));
    }

    @Test
    void naoEnviaDroneSemSerie() {
        long[] p = pedidoDeDroneEmSeparacao();
        Fixtures.mudarStatus(p[0], Fixtures.PEDIDO_ENVIADO).then().statusCode(409);
    }

    @Test
    void quantidadeDeUnidadesTemQueBaterComOItem() {
        long[] p = pedidoDeDroneEmSeparacao();
        long u1 = novaUnidade(p[2]);
        vincular(p[0], p[1], List.of(u1)).statusCode(400).body("errors.field", hasItem("idsUnidades"));
        vincular(p[0], p[1], List.of(u1, u1)).statusCode(400).body("errors.field", hasItem("idsUnidades"));
    }

    @Test
    void unidadeDeOutroProdutoNaoServe() {
        long[] p = pedidoDeDroneEmSeparacao();
        long outra = novaUnidade(Fixtures.criarDrone(3000.0, 1).id());
        vincular(p[0], p[1], List.of(novaUnidade(p[2]), outra)).statusCode(400)
                .body("errors.field", hasItem("idsUnidades"));
    }

    @Test
    void comSeriesOPedidoSaiEAsUnidadesViramVendidas() {
        long[] p = pedidoDeDroneEmSeparacao();
        long u1 = novaUnidade(p[2]);
        long u2 = novaUnidade(p[2]);
        vincular(p[0], p[1], List.of(u1, u2)).statusCode(204);
        Fixtures.mudarStatus(p[0], Fixtures.PEDIDO_ENVIADO).then().statusCode(204);
        given().queryParam("idProduto", p[2]).when().get("/unidades")
                .then().statusCode(200)
                .body("items.status.nome", everyItem(equalTo("VENDIDA")))
                .body("items.validadeGarantia", everyItem(notNullValue()));
    }
}

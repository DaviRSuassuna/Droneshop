package br.com.droneshop.devolucao;

import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #37 Fluxo de devolução e troca com política configurada (RF30, RF31, RF32, RN09, D8, D30).
 * Contrato: docs/issues/037.md
 */
@QuarkusTest
@Tag("issue-37")
class DevolucaoResourceTest {

    static final long SOLICITADA = 1, EM_ANALISE = 2, APROVADA = 3, RECUSADA = 4, POSTAGEM_REVERSA = 5,
            RECEBIDA = 6, REEMBOLSADA = 7, CONCLUIDA = 8;
    static final long REEMBOLSO = 1, TROCA = 2;

    record Entregue(Fixtures.Cliente cliente, long pedido, long item, long produto) {
    }

    static Entregue pedidoEntregue() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long v = Fixtures.criarAcessorio(200.0, 5).id();
        long pedido = Fixtures.pedidoPago(c, v, 1);
        Fixtures.avancarAteEntregue(pedido);
        long item = given().when().get("/pedidos/{id}", pedido).then().statusCode(200)
                .extract().jsonPath().getLong("itens[0].id");
        return new Entregue(c, pedido, item, v);
    }

    static ValidatableResponse solicitar(String token, long pedido, long item, long resolucao) {
        Map<String, Object> d = new HashMap<>();
        d.put("idPedido", pedido);
        d.put("idResolucao", resolucao);
        d.put("motivo", "Arrependimento");
        d.put("itens", List.of(Map.of("idItemPedido", item, "quantidade", 1)));
        return como(token).contentType(ContentType.JSON).body(d).when().post("/conta/devolucoes").then();
    }

    static ValidatableResponse status(long devolucao, long idStatus) {
        return given().contentType(ContentType.JSON).body(Map.of("idStatus", idStatus, "observacao", "teste"))
                .when().patch("/admin/devolucoes/{id}/status", devolucao).then();
    }

    @Test
    void fluxoCompletoDeReembolso() {
        Entregue e = pedidoEntregue();
        long d = solicitar(e.cliente().token(), e.pedido(), e.item(), REEMBOLSO).statusCode(200)
                .body("status.nome", equalTo("SOLICITADA"))
                .extract().jsonPath().getLong("id");
        status(d, EM_ANALISE).statusCode(204);
        status(d, APROVADA).statusCode(204);
        given().when().get("/pedidos/{id}", e.pedido()).then().body("status.nome", equalTo("EM_DEVOLUCAO"));
        status(d, POSTAGEM_REVERSA).statusCode(204);
        status(d, RECEBIDA).statusCode(204);
        given().queryParam("idProduto", e.produto()).queryParam("idTipo", Fixtures.MOV_DEVOLUCAO)
                .when().get("/estoque/movimentos").then().statusCode(200).body("totalItems", equalTo(1));
        status(d, REEMBOLSADA).statusCode(204);
        given().when().get("/pedidos/{id}/pagamentos", e.pedido()).then().statusCode(200)
                .body("find { it.vigente }.status.nome", equalTo("ESTORNADO_PARCIAL"));
        status(d, CONCLUIDA).statusCode(204);
        given().when().get("/pedidos/{id}", e.pedido()).then().body("status.nome", equalTo("DEVOLVIDO"));
        given().when().get("/admin/devolucoes/{id}", d).then().statusCode(200)
                .body("historico", hasSize(7))
                .body("historico[0].origem.nome", equalTo("MANUAL"));
    }

    @Test
    void transicaoInvalidaResponde409() {
        Entregue e = pedidoEntregue();
        long d = solicitar(e.cliente().token(), e.pedido(), e.item(), TROCA).statusCode(200)
                .extract().jsonPath().getLong("id");
        status(d, RECEBIDA).statusCode(409);
        status(d, EM_ANALISE).statusCode(204);
        status(d, RECUSADA).statusCode(204);
        status(d, APROVADA).statusCode(409);
    }

    @Test
    void pedidoNaoEntregueResponde409() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(200.0, 5).id(), 1);
        long item = given().when().get("/pedidos/{id}", pedido).then().extract().jsonPath().getLong("itens[0].id");
        solicitar(c.token(), pedido, item, REEMBOLSO).statusCode(409);
    }

    /** A carga (#16) tem pedidos ENTREGUE do cliente semente entregues há 60 dias. */
    @Test
    void foraDoPrazoDeArrependimentoEhRecusada() {
        String token = Api.login(Api.CLIENTE_EMAIL, Api.CLIENTE_SENHA);
        long pedido = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_ENTREGUE);
        long item = given().when().get("/pedidos/{id}", pedido).then().extract().jsonPath().getLong("itens[0].id");
        solicitar(token, pedido, item, REEMBOLSO).statusCode(400).body("errors.field", hasItem("idPedido"));
    }

    @Test
    void itemDeOutroPedidoOuQuantidadeAcimaResponde400() {
        Entregue e = pedidoEntregue();
        Entregue outro = pedidoEntregue();
        solicitar(e.cliente().token(), e.pedido(), outro.item(), REEMBOLSO).statusCode(400)
                .body("errors.field", hasItem("itens"));
        Map<String, Object> d = new HashMap<>();
        d.put("idPedido", e.pedido());
        d.put("idResolucao", REEMBOLSO);
        d.put("motivo", "Quero devolver dois");
        d.put("itens", List.of(Map.of("idItemPedido", e.item(), "quantidade", 2)));
        como(e.cliente().token()).contentType(ContentType.JSON).body(d).when().post("/conta/devolucoes")
                .then().statusCode(400).body("errors.field", hasItem("itens"));
    }
}

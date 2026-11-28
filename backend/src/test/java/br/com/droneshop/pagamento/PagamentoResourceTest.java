package br.com.droneshop.pagamento;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #27 Pagamento via Mercado Pago sandbox com webhook (RF22, RF23, RF13, D2, D34).
 * Contrato: docs/issues/027.md. O gateway é o MercadoPagoFalso.
 */
@QuarkusTest
@Tag("issue-27")
class PagamentoResourceTest {

    @Test
    void semTokenDa401() {
        anonimo().when().get("/pedidos/{id}/pagamentos", Dados.ID_INEXISTENTE).then().statusCode(401);
    }

    @Test
    void clienteRecebe403EmRotaAdministrativa() {
        Fixtures.Cliente cliente = Fixtures.novoCliente();
        como(cliente.token()).when().get("/pedidos/{id}/pagamentos", Dados.ID_INEXISTENTE).then().statusCode(403);
    }

    static int estoque(long idProduto) {
        return given().when().get("/produtos/{id}", idProduto).then().statusCode(200)
                .extract().jsonPath().getInt("estoqueFisico");
    }

    static void webhook(String gatewayId) {
        anonimo().contentType(ContentType.JSON)
                .body(Map.of("type", "payment", "data", Map.of("id", gatewayId)))
                .when().post("/webhooks/mercadopago").then().statusCode(200);
    }

    @Test
    void listaMetodosEStatus() {
        anonimo().when().get("/metodos-pagamento").then().statusCode(200)
                .body("nome", contains("PIX", "CARTAO", "BOLETO", "FATURAMENTO_PJ"));
        anonimo().when().get("/status-pagamento").then().statusCode(200)
                .body("nome", contains("PENDENTE", "APROVADO", "RECUSADO", "ESTORNADO", "ESTORNADO_PARCIAL"));
    }

    @Test
    void cartaoAprovadoPagaOPedidoEBaixaOEstoque() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long v = Fixtures.criarAcessorio(300.0, 10).id();
        long pedido = Fixtures.pedidoPendente(c, v, 2, Fixtures.CARTAO);
        Fixtures.pagarComCartao(c, pedido, MercadoPagoFalso.CARTAO_APROVADO).then().statusCode(200)
                .body("status.nome", equalTo("APROVADO"))
                .body("vigente", equalTo(true))
                .body("gatewayTransacaoId", notNullValue());
        given().when().get("/pedidos/{id}", pedido).then().statusCode(200).body("status.nome", equalTo("PAGO"));
        org.junit.jupiter.api.Assertions.assertEquals(8, estoque(v));
        given().queryParam("idProduto", v).queryParam("idTipo", Fixtures.MOV_SAIDA).when().get("/estoque/movimentos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].origem.nome", equalTo("SISTEMA"))
                .body("items[0].autor", nullValue());
        given().queryParam("idProduto", v).when().get("/estoque/reservas")
                .then().statusCode(200).body("totalItems", equalTo(0));
    }

    @Test
    void recusaENovaTentativaTrocamOVigente() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(300.0, 10).id(), 1, Fixtures.CARTAO);
        Fixtures.pagarComCartao(c, pedido, MercadoPagoFalso.CARTAO_RECUSADO).then().statusCode(200)
                .body("status.nome", equalTo("RECUSADO"));
        given().when().get("/pedidos/{id}", pedido).then().statusCode(200).body("status.nome", equalTo("PENDENTE"));
        Fixtures.pagarComCartao(c, pedido, MercadoPagoFalso.CARTAO_APROVADO).then().statusCode(200);
        given().when().get("/pedidos/{id}/pagamentos", pedido).then().statusCode(200)
                .body("$", hasSize(2))
                .body("findAll { it.vigente }", hasSize(1))
                .body("find { it.vigente }.status.nome", equalTo("APROVADO"));
    }

    @Test
    void pixFicaPendenteAteOWebhookQueEIdempotente() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long v = Fixtures.criarAcessorio(300.0, 10).id();
        long pedido = Fixtures.pedidoPendente(c, v, 1, Fixtures.PIX);
        String gid = como(c.token()).contentType(ContentType.JSON).body(Map.of("idMetodo", Fixtures.PIX))
                .when().post("/pedidos/{id}/pagamentos", pedido)
                .then().statusCode(200).body("status.nome", equalTo("PENDENTE"))
                .extract().jsonPath().getString("gatewayTransacaoId");

        anonimo().contentType(ContentType.JSON).body(Map.of("status", "approved"))
                .when().post("/mercadopago-falso/teste/{id}/status", gid).then().statusCode(204);
        webhook(gid);
        webhook(gid);

        given().when().get("/pedidos/{id}", pedido).then().statusCode(200).body("status.nome", equalTo("PAGO"));
        given().when().get("/pedidos/{id}/historico", pedido).then().statusCode(200)
                .body("findAll { it.statusNovo.nome == 'PAGO' }", hasSize(1))
                .body("find { it.statusNovo.nome == 'PAGO' }.origem.nome", equalTo("INTEGRACAO"))
                .body("find { it.statusNovo.nome == 'PAGO' }.autor", nullValue());
        org.junit.jupiter.api.Assertions.assertEquals(9, estoque(v));
    }

    @Test
    void pedidoJaPagoResponde409() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(300.0, 10).id(), 1);
        Fixtures.pagarComCartao(c, pedido, MercadoPagoFalso.CARTAO_APROVADO).then().statusCode(409);
    }

    @Test
    void gatewayForaDoArResponde503() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(300.0, 10).id(), 1, Fixtures.CARTAO);
        Fixtures.pagarComCartao(c, pedido, MercadoPagoFalso.GATEWAY_FORA).then().statusCode(503)
                .body("type", endsWith("/errors/external-service-unavailable"));
    }

    @Test
    void faturamentoPjNaoValeParaPf() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(300.0, 10).id(), 1, Fixtures.PIX);
        como(c.token()).contentType(ContentType.JSON).body(Map.of("idMetodo", Fixtures.FATURAMENTO_PJ))
                .when().post("/pedidos/{id}/pagamentos", pedido)
                .then().statusCode(400).body("errors.field", hasItem("idMetodo"));
    }

    @Test
    void outroClienteNaoPagaOPedido() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPendente(dono, Fixtures.criarAcessorio(300.0, 10).id(), 1, Fixtures.CARTAO);
        Fixtures.pagarComCartao(outro, pedido, MercadoPagoFalso.CARTAO_APROVADO).then().statusCode(404);
    }
}

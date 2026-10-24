package br.com.droneshop.pedido;

import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

/**
 * Issue nova "Painel de pedidos: indicadores, busca por CPF/CNPJ, filtro por pagamento e etiquetas em lote".
 * Contrato: docs/issues/nova-pedidos-painel.md. A etiqueta é o nome do arquivo.
 * Busca, indicadores e etiquetas usam os pedidos da carga (#16): cliente semente com CPF 529.982.247-25.
 */
@QuarkusTest
@Tag("issue-pedidos-painel")
class PainelPedidosResourceTest {

    static final String CPF_CLIENTE_SEMENTE = "52998224725";

    static int quantidadeNoStatus(long idStatus) {
        return given().when().get("/pedidos/resumo-status").then().statusCode(200)
                .extract().jsonPath().getInt("find { it.status.id == " + idStatus + " }.quantidade");
    }

    @Test
    void indicadoresBatemComAContagemPorStatus() {
        JsonPath j = given().when().get("/pedidos/indicadores").then().statusCode(200)
                .body("aguardandoPagamento.valorEmAberto", notNullValue())
                .body("devolucoesAbertas", greaterThanOrEqualTo(0))
                .extract().jsonPath();
        assertEquals(quantidadeNoStatus(Fixtures.PEDIDO_PAGO), j.getInt("aSeparar"));
        assertEquals(quantidadeNoStatus(Fixtures.PEDIDO_PENDENTE), j.getInt("aguardandoPagamento.quantidade"));
        assertEquals(quantidadeNoStatus(Fixtures.PEDIDO_ENVIADO), j.getInt("emTransito.quantidade"));
    }

    @Test
    void buscaPorCpfComOuSemMascara() {
        for (String cpf : List.of(CPF_CLIENTE_SEMENTE, "529.982.247-25"))
            given().queryParam("busca", cpf).queryParam("pageSize", 100)
                    .when().get("/pedidos")
                    .then().statusCode(200)
                    .body("items.size()", greaterThan(0))
                    .body("items.cliente.nome", everyItem(equalTo("Cliente Semente")));
    }

    @Test
    void buscaPorCpfDeOutroClienteNaoTrazOSemente() {
        given().queryParam("busca", Fixtures.cpfValido()).queryParam("pageSize", 100)
                .when().get("/pedidos")
                .then().statusCode(200)
                .body("items.cliente.nome", not(hasItem("Cliente Semente")));
    }

    @Test
    void filtraPeloMetodoDoPagamentoVigenteEMostraNaListagem() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long idProduto = Fixtures.criarAcessorio(300.0, 5).id();
        long pix = Fixtures.pedidoPendente(c, idProduto, 1, Fixtures.PIX);
        como(c.token()).contentType(ContentType.JSON).body(Map.of("idMetodo", Fixtures.PIX))
                .when().post("/pedidos/{id}/pagamentos", pix).then().statusCode(200);
        long cartao = Fixtures.pedidoPago(c, idProduto, 1);
        String nome = c.email().split("@")[0];

        given().queryParam("idMetodoPagamento", Fixtures.CARTAO).queryParam("busca", nome).queryParam("pageSize", 100)
                .when().get("/pedidos")
                .then().statusCode(200)
                .body("items.id", hasItem((int) cartao))
                .body("items.id", not(hasItem((int) pix)))
                .body("items.find { it.id == " + cartao + " }.metodoPagamento.id", equalTo((int) Fixtures.CARTAO));
        given().queryParam("idMetodoPagamento", Fixtures.PIX).queryParam("busca", nome).queryParam("pageSize", 100)
                .when().get("/pedidos")
                .then().statusCode(200)
                .body("items.id", hasItem((int) pix))
                .body("items.id", not(hasItem((int) cartao)));
    }

    @Test
    void etiquetasEmLoteGeramUmPdf() {
        long pago = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_PAGO);
        long separacao = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_SEPARACAO);
        byte[] pdf = given().contentType(ContentType.JSON).body(Map.of("ids", List.of(pago, separacao)))
                .when().post("/pedidos/etiquetas")
                .then().statusCode(200).contentType("application/pdf")
                .extract().asByteArray();
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void etiquetasSemIdsDa400EComInexistenteDa404() {
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of()))
                .when().post("/pedidos/etiquetas")
                .then().statusCode(400)
                .body("errors.field", hasItem("ids"));
        long pago = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_PAGO);
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of(pago, Dados.ID_INEXISTENTE)))
                .when().post("/pedidos/etiquetas")
                .then().statusCode(404);
    }
}

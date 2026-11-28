package br.com.droneshop.pedido;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

/**
 * #16 Pedido: modelo, itens, endereço congelado e telas do admin (D12, D19).
 * Contrato: docs/issues/016.md. Usa os pedidos da carga de dados (três por status).
 */
@QuarkusTest
@Tag("issue-16")
class PedidoResourceTest {

    @Test
    void semTokenDa401() {
        anonimo().when().get("/pedidos").then().statusCode(401);
    }

    @Test
    void clienteRecebe403EmRotaAdministrativa() {
        Fixtures.Cliente cliente = Fixtures.novoCliente();
        como(cliente.token()).when().get("/pedidos").then().statusCode(403);
    }

    @Test
    void listaOsStatusDePedido() {
        given().when().get("/status-pedido").then().statusCode(200)
                .body("nome", contains("PENDENTE", "PAGO", "SEPARACAO", "ENVIADO", "ENTREGUE",
                        "EM_DEVOLUCAO", "DEVOLVIDO", "CANCELADO"));
    }

    @Test
    void cargaTemUmPedidoEmCadaStatus() {
        given().when().get("/pedidos/resumo-status")
                .then().statusCode(200)
                .body("$", hasSize(8))
                .body("quantidade", everyItem(greaterThanOrEqualTo(1)));
    }

    @Test
    void filtraPorStatus() {
        given().queryParam("idStatus", Fixtures.PEDIDO_ENVIADO).when().get("/pedidos")
                .then().statusCode(200)
                .body("items.status.nome", everyItem(equalTo("ENVIADO")))
                .body("items[0].numero", matchesPattern("AER-\\d{6}"));
    }

    @Test
    void filtraPorPeriodo() {
        String hoje = LocalDate.now().toString();
        given().queryParam("dataInicio", "2000-01-01").queryParam("dataFim", hoje).when().get("/pedidos")
                .then().statusCode(200).body("totalItems", greaterThanOrEqualTo(8));
        given().queryParam("dataInicio", "2000-01-01").queryParam("dataFim", "2000-01-02").when().get("/pedidos")
                .then().statusCode(200).body("totalItems", equalTo(0));
    }

    @Test
    void detalheTrazItensEEnderecoCongelado() {
        long id = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_PAGO);
        given().when().get("/pedidos/{id}", id)
                .then().statusCode(200)
                .body("itens[0].precoUnitarioCongelado", notNullValue())
                .body("itens[0].produto.sku", notNullValue())
                .body("enderecoEntrega.cep", matchesPattern("\\d{8}"))
                .body("enderecoEntrega.uf", notNullValue())
                .body("cliente.nome", notNullValue());
        given().when().get("/pedidos/{id}", Dados.ID_INEXISTENTE).then().statusCode(404);
    }

    @Test
    void mudarPrecoDoProdutoNaoMudaOPedido() {
        long id = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_PENDENTE);
        JsonPath antes = given().when().get("/pedidos/{id}", id).then().statusCode(200).extract().jsonPath();
        long idProduto = antes.getLong("itens[0].produto.id");
        float precoCongelado = antes.getFloat("itens[0].precoUnitarioCongelado");

        Map<String, Object> corpo = Fixtures.corpoDoProduto(idProduto);
        corpo.put("preco", precoCongelado * 2);
        Fixtures.alterarProduto(idProduto, corpo).then().statusCode(204);

        given().when().get("/pedidos/{id}", id)
                .then().statusCode(200)
                .body("itens[0].precoUnitarioCongelado", equalTo(precoCongelado));
    }

    @Test
    void pedidoNaoTemCadastroDireto() {
        given().contentType(ContentType.JSON).body(Map.of()).when().post("/pedidos").then().statusCode(405);
    }
}

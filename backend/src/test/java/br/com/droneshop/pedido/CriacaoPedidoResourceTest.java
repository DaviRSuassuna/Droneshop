package br.com.droneshop.pedido;

import static br.com.droneshop.testes.Api.carrinho;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
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
 * #29 Criação do pedido com descontos e uso de cupom (RF52, RF53, RF54, RN13, RN16, D9).
 * Contrato: docs/issues/029.md
 */
@QuarkusTest
@Tag("issue-29")
class CriacaoPedidoResourceTest {

    static String agora(int dias) {
        return LocalDateTime.now().plusDays(dias).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    /** Cupom percentual vigente; devolve [id, código]. */
    static Object[] cupom(double percentual, int limiteTotal) {
        Map<String, Object> c = new HashMap<>();
        String codigo = "PED" + Dados.sufixo().toUpperCase();
        c.put("codigo", codigo);
        c.put("idTipoDesconto", 1);
        c.put("valor", percentual);
        c.put("dataInicio", agora(-1));
        c.put("dataFim", agora(30));
        c.put("valorMinimo", 0);
        c.put("limiteTotalUso", limiteTotal);
        c.put("limitePorCliente", 1);
        c.put("idsCategorias", List.of());
        c.put("idsMarcas", List.of());
        c.put("idsVariantes", List.of());
        long id = Fixtures.criar("/cupons", c);
        return new Object[] { id, codigo };
    }

    /** Fluxo completo com cupom opcional; devolve o id do pedido. */
    static long comprar(Fixtures.Cliente cli, long idVariante, String codigoCupom) {
        String c = Fixtures.novoCarrinho();
        Fixtures.adicionar(c, idVariante, 1);
        carrinho(cli.token(), c).when().post("/carrinho/vincular").then().statusCode(200);
        if (codigoCupom != null)
            carrinho(cli.token(), c).contentType(ContentType.JSON).body(Map.of("codigo", codigoCupom))
                    .when().post("/carrinho/cupom").then().statusCode(200);
        carrinho(cli.token(), c).when().post("/carrinho/reservar").then().statusCode(200);
        long idEndereco = Fixtures.enderecoCliente(cli);
        carrinho(cli.token(), c).contentType(ContentType.JSON).body(Map.of("cep", Fixtures.CEP_DESTINO))
                .when().post("/frete/cotacao").then().statusCode(200);
        Map<String, Object> b = new HashMap<>();
        b.put("idEndereco", idEndereco);
        b.put("idModalidadeEntrega", Fixtures.ENTREGA_PADRAO);
        b.put("codigoServicoTransportadora", "PAC");
        b.put("idMetodoPagamento", Fixtures.PIX);
        return carrinho(cli.token(), c).contentType(ContentType.JSON).body(b).when().post("/checkout")
                .then().statusCode(200).extract().jsonPath().getLong("id");
    }

    @Test
    void listaAsOrigensDeDesconto() {
        given().when().get("/origens-desconto").then().statusCode(200).body("nome", contains("CUPOM", "PROMOCAO"));
    }

    @Test
    void cupomViraDescontoDoPedidoEUso() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        Object[] cupom = cupom(10, 100);
        long pedido = comprar(cli, Fixtures.criarAcessorio(1000.0, 5).idVariante(), (String) cupom[1]);
        JsonPath j = como(cli.token()).when().get("/conta/pedidos/{id}", pedido)
                .then().statusCode(200)
                .body("valorProdutos", equalTo(1000.0f))
                .body("valorDesconto", equalTo(100.0f))
                .body("descontos[0].origem.nome", equalTo("CUPOM"))
                .body("descontos[0].valor", equalTo(100.0f))
                .extract().jsonPath();
        float esperado = j.getFloat("valorProdutos") + j.getFloat("valorFrete") - j.getFloat("valorDesconto");
        org.junit.jupiter.api.Assertions.assertEquals(esperado, j.getFloat("valorTotal"), 0.01f);
        given().when().get("/cupons/{id}/usos", cupom[0]).then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].pedido.id", equalTo((int) pedido));
    }

    @Test
    void promocaoVigenteViraDescontoDeOrigemPromocao() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        long v = Fixtures.criarAcessorio(1000.0, 5).idVariante();
        Map<String, Object> promo = new HashMap<>();
        promo.put("nome", "Promo " + Dados.sufixo());
        promo.put("idEscopo", 1);
        promo.put("dataInicio", agora(-1));
        promo.put("dataFim", agora(5));
        promo.put("percentualDesconto", 20);
        promo.put("ativa", true);
        promo.put("idsVariantes", List.of(v));
        Fixtures.criar("/promocoes", promo);
        long pedido = comprar(cli, v, null);
        como(cli.token()).when().get("/conta/pedidos/{id}", pedido)
                .then().statusCode(200)
                .body("descontos.origem.nome", hasItem("PROMOCAO"))
                .body("valorDesconto", equalTo(200.0f));
    }

    @Test
    void cancelarOPedidoLiberaOUsoDoCupom() {
        Object[] cupom = cupom(10, 1);
        Fixtures.Cliente primeiro = Fixtures.novoCliente();
        long pedido = comprar(primeiro, Fixtures.criarAcessorio(1000.0, 5).idVariante(), (String) cupom[1]);
        Fixtures.mudarStatus(pedido, Fixtures.PEDIDO_CANCELADO).then().statusCode(204);
        given().when().get("/cupons/{id}/usos", cupom[0]).then().statusCode(200).body("totalItems", equalTo(0));

        String outro = Fixtures.novoCarrinho();
        Fixtures.adicionar(outro, Fixtures.criarAcessorio(1000.0, 5).idVariante(), 1);
        carrinho(outro).contentType(ContentType.JSON).body(Map.of("codigo", cupom[1]))
                .when().post("/carrinho/cupom").then().statusCode(200);
    }

    @Test
    void meusPedidosSoDoCliente() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        long pedido = comprar(dono, Fixtures.criarAcessorio(100.0, 5).idVariante(), null);
        como(dono.token()).when().get("/conta/pedidos").then().statusCode(200)
                .body("totalItems", equalTo(1)).body("items[0].id", equalTo((int) pedido));
        como(outro.token()).when().get("/conta/pedidos/{id}", pedido).then().statusCode(404);
    }
}

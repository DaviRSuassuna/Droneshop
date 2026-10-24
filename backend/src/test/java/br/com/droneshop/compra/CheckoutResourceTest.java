package br.com.droneshop.compra;

import static br.com.droneshop.testes.Api.carrinho;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.pedido.FreteFalso;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #26 Checkout: endereço, entrega, pagamento e confirmação (RF21, RNF05).
 * Contrato: docs/issues/026.md
 */
@QuarkusTest
@Tag("issue-26")
class CheckoutResourceTest {

    record Preparado(Fixtures.Cliente cliente, String carrinho, long idEndereco, long idProduto) {
    }

    /** Cliente logado com carrinho vinculado, um item e reserva feita. */
    static Preparado preparar(boolean reservar) {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        long v = Fixtures.criarAcessorio(250.0, 10).id();
        String c = Fixtures.novoCarrinho();
        Fixtures.adicionar(c, v, 2);
        carrinho(cli.token(), c).when().post("/carrinho/vincular").then().statusCode(200);
        if (reservar)
            carrinho(cli.token(), c).when().post("/carrinho/reservar").then().statusCode(200);
        return new Preparado(cli, c, Fixtures.enderecoCliente(cli), v);
    }

    static Map<String, Object> corpo(long idEndereco, long idModalidade) {
        Map<String, Object> b = new HashMap<>();
        b.put("idEndereco", idEndereco);
        b.put("idModalidadeEntrega", idModalidade);
        b.put("codigoServicoTransportadora", idModalidade == Fixtures.ENTREGA_PADRAO ? "PAC" : null);
        b.put("idMetodoPagamento", Fixtures.PIX);
        return b;
    }

    static ValidatableResponse checkout(Preparado p, Map<String, Object> corpo) {
        return carrinho(p.cliente().token(), p.carrinho()).contentType(ContentType.JSON).body(corpo)
                .when().post("/checkout").then();
    }

    static void cotar(Preparado p) {
        carrinho(p.cliente().token(), p.carrinho()).contentType(ContentType.JSON)
                .body(Map.of("cep", Fixtures.CEP_DESTINO)).when().post("/frete/cotacao").then().statusCode(200);
    }

    @Test
    void checkoutCriaPedidoPendenteEConverteOCarrinho() {
        Preparado p = preparar(true);
        cotar(p);
        long id = checkout(p, corpo(p.idEndereco(), Fixtures.ENTREGA_PADRAO)).statusCode(200)
                .body("status.nome", equalTo("PENDENTE"))
                .body("itens", hasSize(1))
                .body("itens[0].quantidade", equalTo(2))
                .body("itens[0].precoUnitarioCongelado", equalTo(250.0f))
                .body("valorProdutos", equalTo(500.0f))
                .body("enderecoEntrega.cep", equalTo(Fixtures.CEP_DESTINO))
                .body("enderecoEntrega.cidade", equalTo("Palmas"))
                .body("entrega.modalidade.nome", equalTo("PADRAO"))
                .extract().jsonPath().getLong("id");
        carrinho(p.carrinho()).when().get("/carrinho").then().statusCode(200)
                .body("status.nome", equalTo("CONVERTIDO"));
        given().when().get("/pedidos/{id}", id).then().statusCode(200).body("status.nome", equalTo("PENDENTE"));
    }

    @Test
    void carrinhoConvertidoNaoGeraSegundoPedido() {
        Preparado p = preparar(true);
        cotar(p);
        checkout(p, corpo(p.idEndereco(), Fixtures.ENTREGA_PADRAO)).statusCode(200);
        checkout(p, corpo(p.idEndereco(), Fixtures.ENTREGA_PADRAO)).statusCode(409);
    }

    @Test
    void semReservaResponde409() {
        Preparado p = preparar(false);
        cotar(p);
        checkout(p, corpo(p.idEndereco(), Fixtures.ENTREGA_PADRAO)).statusCode(409);
    }

    @Test
    void enderecoDeOutroClienteResponde400() {
        Preparado p = preparar(true);
        long alheio = Fixtures.enderecoCliente(Fixtures.novoCliente());
        cotar(p);
        checkout(p, corpo(alheio, Fixtures.ENTREGA_PADRAO)).statusCode(400)
                .body("errors.field", hasItem("idEndereco"));
    }

    @Test
    void semLoginResponde401() {
        Preparado p = preparar(true);
        carrinho(p.carrinho()).contentType(ContentType.JSON).body(corpo(p.idEndereco(), Fixtures.ENTREGA_PADRAO))
                .when().post("/checkout").then().statusCode(401);
    }

    @Test
    void agregadorForaDoArNaoTravaACompra() {
        Preparado p = preparar(true);
        Map<String, Object> e = new HashMap<>();
        e.put("apelido", "Longe");
        e.put("cep", FreteFalso.CEP_AGREGADOR_FORA);
        e.put("logradouro", "Rua Sem Agregador");
        e.put("numero", "1");
        e.put("bairro", "Centro");
        e.put("idMunicipio", Fixtures.idPalmas());
        e.put("principal", false);
        long idEndereco = como(p.cliente().token()).contentType(ContentType.JSON).body(e)
                .when().post("/conta/enderecos").then().statusCode(200).extract().jsonPath().getLong("id");
        carrinho(p.cliente().token(), p.carrinho()).contentType(ContentType.JSON)
                .body(Map.of("cep", FreteFalso.CEP_AGREGADOR_FORA)).when().post("/frete/cotacao")
                .then().statusCode(200);
        Map<String, Object> b = corpo(idEndereco, Fixtures.ENTREGA_PADRAO);
        b.put("codigoServicoTransportadora", null);
        checkout(p, b).statusCode(200).body("entrega.origemCalculo.nome", equalTo("REGRA_FRETE"));
    }
}

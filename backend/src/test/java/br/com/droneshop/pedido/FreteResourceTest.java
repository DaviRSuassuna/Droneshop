package br.com.droneshop.pedido;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #30 Frete híbrido: agregador + RegraFrete de fallback (RF27, RF57, RN07, D4, RNF05).
 * Contrato: docs/issues/030.md. O agregador é o FreteFalso.
 */
@QuarkusTest
@Tag("issue-30")
class FreteResourceTest {

    static ValidatableResponse cotar(String cep, long idProduto, int quantidade) {
        return anonimo().contentType(ContentType.JSON)
                .body(Map.of("cep", cep, "itens", List.of(Map.of("idProduto", idProduto, "quantidade", quantidade))))
                .when().post("/frete/cotacao").then();
    }

    static float pesoCotado() {
        return anonimo().when().get("/frete-falso/teste/ultima-requisicao").then().statusCode(200)
                .extract().jsonPath().getFloat("package.weight");
    }

    @Test
    void listaModalidadesEOrigens() {
        anonimo().when().get("/modalidades-entrega").then().statusCode(200)
                .body("nome", contains("EXPRESSA", "PADRAO", "RETIRADA_SHOWROOM"));
        anonimo().when().get("/origens-calculo-frete").then().statusCode(200)
                .body("nome", contains("INTEGRACAO_AGREGADOR", "REGRA_FRETE"));
    }

    @Test
    void cotaPeloAgregadorEIncluiRetirada() {
        long v = Fixtures.criarAcessorio(100.0, 5).id();
        cotar(Fixtures.CEP_DESTINO, v, 1).statusCode(200)
                .body("modalidade.nome", hasItems("EXPRESSA", "PADRAO", "RETIRADA_SHOWROOM"))
                .body("find { it.modalidade.nome == 'PADRAO' }.origemCalculo.nome", equalTo("INTEGRACAO_AGREGADOR"))
                .body("find { it.modalidade.nome == 'PADRAO' }.codigoServicoTransportadora", equalTo("PAC"))
                .body("find { it.modalidade.nome == 'EXPRESSA' }.codigoServicoTransportadora", equalTo("SEDEX"))
                .body("find { it.modalidade.nome == 'RETIRADA_SHOWROOM' }.origemCalculo.nome", equalTo("REGRA_FRETE"));
    }

    @Test
    void agregadorForaDoArCaiParaARegraFrete() {
        long v = Fixtures.criarAcessorio(100.0, 5).id();
        cotar(FreteFalso.CEP_AGREGADOR_FORA, v, 1).statusCode(200)
                .body("find { it.modalidade.nome == 'PADRAO' }.origemCalculo.nome", equalTo("REGRA_FRETE"))
                .body("find { it.modalidade.nome == 'EXPRESSA' }.origemCalculo.nome", equalTo("REGRA_FRETE"));
    }

    @Test
    void cepInvalidoResponde400() {
        long v = Fixtures.criarAcessorio(100.0, 5).id();
        cotar("7700", v, 1).statusCode(400).body("errors.field", hasItem("cep"));
    }

    @Test
    void pesoDaEmbalagemSomaAsQuantidades() {
        long v = Fixtures.criarAcessorio(100.0, 5).id(); // 1,5 kg na fixture
        cotar(Fixtures.CEP_DESTINO, v, 2).statusCode(200);
        org.junit.jupiter.api.Assertions.assertEquals(3.0f, pesoCotado(), 0.001f);
    }

    @Test
    void kitCotaComPesoDerivadoDosComponentes() {
        long drone = Fixtures.criarDrone(1000.0, 5).id();
        long bateria = Fixtures.criarAcessorio(100.0, 5).id();
        long idKit = Fixtures.criarKit(Fixtures.componente(drone, 1), Fixtures.componente(bateria, 2)).id();
        cotar(Fixtures.CEP_DESTINO, idKit, 1).statusCode(200);
        org.junit.jupiter.api.Assertions.assertEquals(4.5f, pesoCotado(), 0.001f);
    }

    @Test
    void pedidoCongelaAEntregaEscolhida() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(100.0, 5).id(), 1, Fixtures.PIX);
        given().when().get("/pedidos/{id}", pedido).then().statusCode(200)
                .body("entrega.modalidade.nome", equalTo("PADRAO"))
                .body("entrega.transportadora", equalTo("Correios"))
                .body("entrega.custoCongelado", notNullValue())
                .body("entrega.prazoCongeladoDias", equalTo(6));
    }

    @Test
    void crudDeRegrasDeFrete() {
        Map<String, Object> r = new HashMap<>();
        r.put("idModalidade", Fixtures.ENTREGA_RETIRADA);
        r.put("custoFixo", 0);
        r.put("prazoDias", 1);
        r.put("cepOrigem", "77001000");
        long id = Fixtures.criar("/admin/regras-frete", r);
        given().when().get("/admin/regras-frete/{id}", id).then().statusCode(200)
                .body("modalidade.nome", equalTo("RETIRADA_SHOWROOM"));
        given().when().delete("/admin/regras-frete/{id}", id).then().statusCode(204);
    }
}

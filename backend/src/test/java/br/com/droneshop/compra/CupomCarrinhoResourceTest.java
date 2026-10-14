package br.com.droneshop.compra;

import static br.com.droneshop.testes.Api.carrinho;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

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
 * #25 Aplicar cupom no carrinho (RF20, RN03, D9).
 * Contrato: docs/issues/025.md
 */
@QuarkusTest
@Tag("issue-25")
class CupomCarrinhoResourceTest {

    static String cupom(Map<String, Object> c) {
        return CupomResourceTest.post(c).statusCode(200).extract().jsonPath().getString("codigo");
    }

    static Map<String, Object> percentual(double valor) {
        return CupomResourceTest.cupom("C" + Dados.sufixo(), CupomResourceTest.PERCENTUAL, valor);
    }

    static ValidatableResponse aplicar(String carrinho, String codigo) {
        return carrinho(carrinho).contentType(ContentType.JSON).body(Map.of("codigo", codigo))
                .when().post("/carrinho/cupom").then();
    }

    static String carrinhoCom(long idVariante, int quantidade) {
        String c = Fixtures.novoCarrinho();
        Fixtures.adicionar(c, idVariante, quantidade);
        return c;
    }

    static void recusa(ValidatableResponse r, String motivo) {
        r.statusCode(400)
                .body("errors.field", hasItem("codigo"))
                .body("errors.find { it.field == 'codigo' }.motivo", equalTo(motivo));
    }

    @Test
    void aplicaCupomPercentual() {
        String codigo = cupom(percentual(10));
        String c = carrinhoCom(Fixtures.criarDrone(1000.0, 5).idVariante(), 1);
        aplicar(c, codigo.toLowerCase()).statusCode(200)
                .body("cupom.codigo", equalTo(codigo))
                .body("cupom.desconto", equalTo(100.0f))
                .body("desconto", equalTo(100.0f))
                .body("total", equalTo(900.0f));
    }

    @Test
    void removerRecalculaOTotal() {
        String codigo = cupom(percentual(10));
        String c = carrinhoCom(Fixtures.criarDrone(1000.0, 5).idVariante(), 1);
        aplicar(c, codigo).statusCode(200);
        carrinho(c).when().delete("/carrinho/cupom")
                .then().statusCode(200)
                .body("cupom", nullValue())
                .body("total", equalTo(1000.0f));
    }

    @Test
    void cupomInexistente() {
        String c = carrinhoCom(Fixtures.criarDrone(1000.0, 5).idVariante(), 1);
        recusa(aplicar(c, "NAOEXISTE" + Dados.sufixo()), "CUPOM_INEXISTENTE");
    }

    @Test
    void foraDaVigencia() {
        Map<String, Object> futuro = percentual(10);
        futuro.put("dataInicio", CupomResourceTest.dias(5));
        futuro.put("dataFim", CupomResourceTest.dias(10));
        String c = carrinhoCom(Fixtures.criarDrone(1000.0, 5).idVariante(), 1);
        recusa(aplicar(c, cupom(futuro)), "FORA_DA_VIGENCIA");
    }

    @Test
    void valorMinimoNaoAtingido() {
        Map<String, Object> minimo = percentual(10);
        minimo.put("valorMinimo", 5000);
        String c = carrinhoCom(Fixtures.criarDrone(1000.0, 5).idVariante(), 1);
        recusa(aplicar(c, cupom(minimo)), "VALOR_MINIMO_NAO_ATINGIDO");
    }

    @Test
    void semItensElegiveisPelaRestricaoDeMarca() {
        Fixtures.Arvore outra = Fixtures.arvore();
        Map<String, Object> restrito = percentual(10);
        restrito.put("idsMarcas", List.of(outra.idMarca()));
        String c = carrinhoCom(Fixtures.criarDrone(1000.0, 5).idVariante(), 1);
        recusa(aplicar(c, cupom(restrito)), "SEM_ITENS_ELEGIVEIS");
    }

    @Test
    void restricaoDescontaSoOsItensElegiveis() {
        Fixtures.Arvore a = Fixtures.arvore();
        long elegivel = Fixtures.criarDrone(a, 1000.0, 5).idVariante();
        long outro = Fixtures.criarDrone(500.0, 5).idVariante();
        Map<String, Object> restrito = percentual(10);
        restrito.put("idsMarcas", List.of(a.idMarca()));
        String c = carrinhoCom(elegivel, 1);
        Fixtures.adicionar(c, outro, 1);
        aplicar(c, cupom(restrito)).statusCode(200)
                .body("desconto", equalTo(100.0f))
                .body("total", equalTo(1400.0f));
    }

    @Test
    void carrinhoQueCaiAbaixoDoMinimoPerdeOCupom() {
        Map<String, Object> minimo = percentual(10);
        minimo.put("valorMinimo", 1500);
        long v = Fixtures.criarDrone(1000.0, 5).idVariante();
        String c = carrinhoCom(v, 2);
        aplicar(c, cupom(minimo)).statusCode(200);
        long idItem = carrinho(c).when().get("/carrinho").then().statusCode(200)
                .extract().jsonPath().getLong("itens[0].id");
        carrinho(c).contentType(ContentType.JSON).body(Map.of("quantidade", 1))
                .when().patch("/carrinho/itens/{id}", idItem)
                .then().statusCode(200)
                .body("cupom", nullValue())
                .body("avisos", contains("CUPOM_REMOVIDO_VALOR_MINIMO"))
                .body("total", equalTo(1000.0f));
    }
}

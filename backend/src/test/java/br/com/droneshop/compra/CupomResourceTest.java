package br.com.droneshop.compra;

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
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #15 Cupom: cadastro com validade, limites e restrições (RF20, RN03).
 * Contrato: docs/issues/015.md
 */
@QuarkusTest
@Tag("issue-15")
public class CupomResourceTest {

    public static final long PERCENTUAL = 1, VALOR_FIXO = 2;

    static String dias(int d) {
        return LocalDateTime.now().plusDays(d).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    public static Map<String, Object> cupom(String codigo, long tipo, double valor) {
        Map<String, Object> c = new HashMap<>();
        c.put("codigo", codigo);
        c.put("idTipoDesconto", tipo);
        c.put("valor", valor);
        c.put("dataInicio", dias(-1));
        c.put("dataFim", dias(30));
        c.put("valorMinimo", 0);
        c.put("limiteTotalUso", 100);
        c.put("limitePorCliente", 1);
        c.put("idsCategorias", List.of());
        c.put("idsMarcas", List.of());
        c.put("idsVariantes", List.of());
        return c;
    }

    public static ValidatableResponse post(Map<String, Object> c) {
        return given().contentType(ContentType.JSON).body(c).when().post("/cupons").then();
    }

    @Test
    void listaOsTiposDeDesconto() {
        given().when().get("/tipos-desconto-cupom").then().statusCode(200)
                .body("nome", contains("PERCENTUAL", "VALOR_FIXO"));
    }

    @Test
    void codigoGuardadoEmMaiusculas() {
        String codigo = "bem" + Dados.sufixo();
        post(cupom(codigo, PERCENTUAL, 10)).statusCode(200)
                .body("codigo", equalTo(codigo.toUpperCase()))
                .body("situacao", equalTo("VIGENTE"))
                .body("usos", equalTo(0));
    }

    @Test
    void codigoRepetidoSemDiferenciarMaiusculas() {
        String codigo = "REP" + Dados.sufixo().toUpperCase();
        post(cupom(codigo, PERCENTUAL, 10)).statusCode(200);
        post(cupom(codigo.toLowerCase(), PERCENTUAL, 10)).statusCode(400)
                .body("errors.field", hasItem("codigo"));
        post(cupom("COM ESPACO", PERCENTUAL, 10)).statusCode(400)
                .body("errors.field", hasItem("codigo"));
    }

    @Test
    void validacoesDeValorDataELimite() {
        post(cupom("P" + Dados.sufixo(), PERCENTUAL, 101)).statusCode(400).body("errors.field", hasItem("valor"));
        post(cupom("V" + Dados.sufixo(), VALOR_FIXO, 0)).statusCode(400).body("errors.field", hasItem("valor"));

        Map<String, Object> datas = cupom("D" + Dados.sufixo(), PERCENTUAL, 10);
        datas.put("dataFim", dias(-5));
        post(datas).statusCode(400).body("errors.field", hasItem("dataFim"));

        Map<String, Object> limite = cupom("L" + Dados.sufixo(), PERCENTUAL, 10);
        limite.put("limiteTotalUso", 1);
        limite.put("limitePorCliente", 2);
        post(limite).statusCode(400).body("errors.field", hasItem("limitePorCliente"));
    }

    @Test
    void crudEUsos() {
        String codigo = "CRUD" + Dados.sufixo().toUpperCase();
        Map<String, Object> c = cupom(codigo, VALOR_FIXO, 50);
        long id = post(c).statusCode(200).extract().jsonPath().getLong("id");
        given().queryParam("codigo", codigo).when().get("/cupons")
                .then().statusCode(200).body("totalItems", equalTo(1));
        c.put("valor", 60);
        given().contentType(ContentType.JSON).body(c).when().put("/cupons/{id}", id).then().statusCode(204);
        given().when().get("/cupons/{id}", id).then().statusCode(200).body("valor", equalTo(60.0f));
        given().when().get("/cupons/{id}/usos", id).then().statusCode(200).body("totalItems", equalTo(0));
        given().when().delete("/cupons/{id}", id).then().statusCode(204);
        given().when().get("/cupons/{id}", id).then().statusCode(404);
    }
}

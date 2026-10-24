package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;

/**
 * #43 Registro de termos buscados e buscas sem resultado (RF10, D23).
 * Contrato: docs/issues/043.md
 */
@QuarkusTest
@Tag("issue-43")
class TermoBuscadoResourceTest {

    static void buscar(String q) {
        anonimo().queryParam("q", q).when().get("/loja/busca").then().statusCode(200);
    }

    static List<Map<String, Object>> relatorio(boolean semResultado) {
        return given().queryParam("semResultado", semResultado).queryParam("pageSize", 500)
                .when().get("/admin/termos-buscados").then().statusCode(200).extract().jsonPath().getList("items");
    }

    @Test
    void buscaIgnoraMaiusculasEAcentos() {
        Fixtures.Arvore a = Fixtures.arvore();
        String s = Dados.sufixo();
        Fixtures.criar("/drones", Fixtures.drone("Fálcon " + s, a,
                Fixtures.dadosComerciais("FA-" + s.toUpperCase(), 100.0, 1)));
        anonimo().queryParam("q", "FALCON " + s.toUpperCase()).when().get("/loja/busca")
                .then().statusCode(200).body("totalItems", equalTo(1));
    }

    @Test
    void gravaOTermoNormalizadoEOrdenaPorFrequencia() {
        String s = Dados.sufixo();
        buscar("Zéfiro " + s);
        buscar("ZEFIRO   " + s);
        buscar("zefiro " + s);
        buscar("outro " + s);
        List<Map<String, Object>> itens = relatorio(true);
        Map<String, Object> zefiro = itens.stream().filter(i -> ("zefiro " + s).equals(i.get("termo"))).findFirst()
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(3, ((Number) zefiro.get("buscas")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(0, ((Number) zefiro.get("ultimaQuantidadeResultados")).intValue());
        int posZefiro = itens.indexOf(zefiro);
        int posOutro = itens.stream().map(i -> i.get("termo")).toList().indexOf("outro " + s);
        org.junit.jupiter.api.Assertions.assertTrue(posZefiro < posOutro, "ordenado por buscas, maior primeiro");
    }

    @Test
    void termoComResultadoNaoApareceNoFiltroSemResultado() {
        Fixtures.Arvore a = Fixtures.arvore();
        String s = Dados.sufixo();
        Fixtures.criar("/drones", Fixtures.drone("Achavel " + s, a,
                Fixtures.dadosComerciais("AH-" + s.toUpperCase(), 100.0, 1)));
        buscar("achavel " + s);
        org.junit.jupiter.api.Assertions.assertTrue(relatorio(true).stream()
                .noneMatch(i -> ("achavel " + s).equals(i.get("termo"))));
        given().queryParam("pageSize", 500).when().get("/admin/termos-buscados")
                .then().statusCode(200).body("items.termo", hasItem("achavel " + s));
    }

    @Test
    void termoMuitoCurtoResponde400() {
        anonimo().queryParam("q", "a").when().get("/loja/busca")
                .then().statusCode(400).body("errors.field", hasItem("q"));
    }
}

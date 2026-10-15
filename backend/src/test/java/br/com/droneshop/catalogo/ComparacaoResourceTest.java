package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.anonimo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;

/**
 * #44 Comparação de especificações entre modelos (RF07, D14).
 * Contrato: docs/issues/044.md
 */
@QuarkusTest
@Tag("issue-44")
class ComparacaoResourceTest {

    static long drone(Fixtures.Arvore a, int autonomia) {
        String s = Dados.sufixo();
        Map<String, Object> d = Fixtures.drone("Comparado " + s, a,
                Fixtures.lista(Fixtures.variante("CP-" + s.toUpperCase(), 1000.0, 1)));
        d.put("autonomiaMinutos", autonomia);
        return Fixtures.criar("/drones", d);
    }

    @Test
    void comparaEDestacaDiferencas() {
        Fixtures.Arvore a = Fixtures.arvore();
        long d1 = drone(a, 46);
        long d2 = drone(a, 31);
        anonimo().queryParam("ids", d1 + "," + d2).when().get("/loja/comparar")
                .then().statusCode(200)
                .body("produtos", hasSize(2))
                .body("produtos.id", contains((int) d1, (int) d2))
                .body("specs.find { it.campo == 'autonomiaMinutos' }.valores", contains(46, 31))
                .body("specs.find { it.campo == 'autonomiaMinutos' }.diferente", equalTo(true))
                .body("specs.find { it.campo == 'autonomiaMinutos' }.melhor", equalTo(0))
                .body("specs.find { it.campo == 'pesoGramas' }.diferente", equalTo(false));
    }

    @Test
    void foraDaFaixaOuAcessorioResponde400() {
        Fixtures.Arvore a = Fixtures.arvore();
        long d1 = drone(a, 46);
        anonimo().queryParam("ids", String.valueOf(d1)).when().get("/loja/comparar")
                .then().statusCode(400).body("errors.field", hasItem("ids"));
        long acessorio = Fixtures.criarAcessorio(a, 100.0, 1).id();
        anonimo().queryParam("ids", d1 + "," + acessorio).when().get("/loja/comparar")
                .then().statusCode(400).body("errors.field", hasItem("ids"));
        String cinco = d1 + "," + drone(a, 1) + "," + drone(a, 2) + "," + drone(a, 3) + "," + drone(a, 4);
        anonimo().queryParam("ids", cinco).when().get("/loja/comparar")
                .then().statusCode(400).body("errors.field", hasItem("ids"));
    }
}

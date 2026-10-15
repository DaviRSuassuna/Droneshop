package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

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

/**
 * #21 Rotas públicas da vitrine que vieram do admin: configuração da loja (#6),
 * acessórios compatíveis (#11) e ofertas (#14). Saíram da Fase 1 para ela
 * ficar só com o painel administrativo.
 * Contrato: docs/issues/021.md
 */
@QuarkusTest
@Tag("issue-21")
class LojaVitrineResourceTest {

    static Map<String, Object> acessorioCompativel(Fixtures.Arvore a, List<Long> ids, long idStatus) {
        Map<String, Object> ac = Fixtures.acessorio("Bateria " + Dados.sufixo(), a,
                Fixtures.lista(Fixtures.variante("AC-" + Dados.sufixo().toUpperCase(), 499.0, 3)));
        ac.put("idsCompativeis", ids);
        ac.put("idStatus", idStatus);
        return ac;
    }

    static String dias(int d) {
        return LocalDateTime.now().plusDays(d).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    static void promocao(long idVariante, int inicio, int fim) {
        Map<String, Object> p = new HashMap<>();
        p.put("nome", "Promo " + Dados.sufixo());
        p.put("idEscopo", 1);
        p.put("dataInicio", dias(inicio));
        p.put("dataFim", dias(fim));
        p.put("percentualDesconto", 10);
        p.put("valorDesconto", null);
        p.put("valorMinimoCarrinho", null);
        p.put("ativa", true);
        p.put("idsVariantes", List.of(idVariante));
        given().contentType(ContentType.JSON).body(p).when().post("/promocoes").then().statusCode(200);
    }

    @Test
    void configuracaoMostraSoOQueALojaExibe() {
        anonimo().when().get("/loja/configuracao")
                .then().statusCode(200)
                .body("prazoArrependimentoDias", equalTo(7))
                .body("dadosFiscais", nullValue());
    }

    @Test
    void compativeisListaSoAcessoriosAtivos() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado drone = Fixtures.criarDrone(a, 7999.0, 2);
        long ativo = Fixtures.criar("/acessorios", acessorioCompativel(a, List.of(drone.id()), Fixtures.PRODUTO_ATIVO));
        Fixtures.criar("/acessorios", acessorioCompativel(a, List.of(drone.id()), Fixtures.PRODUTO_INATIVO));

        anonimo().when().get("/loja/produtos/{slug}/compativeis", drone.slug())
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo((int) ativo));
    }

    @Test
    void compativeisDeSlugInexistenteResponde404() {
        anonimo().when().get("/loja/produtos/{slug}/compativeis", "nao-existe-" + Dados.sufixo())
                .then().statusCode(404);
    }

    @Test
    void ofertasMostramSoAsVigentes() {
        Fixtures.ProdutoCriado vigente = Fixtures.criarDrone(1000.0, 5);
        Fixtures.ProdutoCriado agendada = Fixtures.criarDrone(1000.0, 5);
        promocao(vigente.idVariante(), -1, 5);
        promocao(agendada.idVariante(), 5, 10);
        anonimo().when().get("/loja/ofertas")
                .then().statusCode(200)
                .body("variante.id", hasItem((int) vigente.idVariante()))
                .body("variante.id", not(hasItem((int) agendada.idVariante())))
                .body("find { it.variante.id == " + vigente.idVariante() + " }.precoPor", equalTo(900.0f));
    }
}

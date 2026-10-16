package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #11 Compatibilidade N:N entre Acessório e Produto (D17).
 * Contrato: docs/issues/011.md
 */
@QuarkusTest
@Tag("issue-11")
class CompatibilidadeResourceTest {

    static Map<String, Object> acessorioCompativel(Fixtures.Arvore a, List<Long> ids, long idStatus) {
        Map<String, Object> ac = Fixtures.acessorio("Bateria " + Dados.sufixo(), a,
                Fixtures.lista(Fixtures.variante("AC-" + Dados.sufixo().toUpperCase(), 499.0, 3)));
        ac.put("idsCompativeis", ids);
        ac.put("idStatus", idStatus);
        return ac;
    }

    @Test
    void vinculoApareceNosDoisLados() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado drone = Fixtures.criarDrone(a, 7999.0, 2);
        long idAcessorio = given().contentType(ContentType.JSON)
                .body(acessorioCompativel(a, List.of(drone.id()), Fixtures.PRODUTO_ATIVO))
                .when().post("/acessorios")
                .then().statusCode(200)
                .body("compativeis", hasSize(1))
                .body("compativeis[0].id", equalTo((int) drone.id()))
                .extract().jsonPath().getLong("id");

        given().when().get("/produtos/{id}", drone.id())
                .then().statusCode(200)
                .body("acessoriosCompativeis.id", hasItem((int) idAcessorio));
    }

    @Test
    void idInexistenteResponde400() {
        given().contentType(ContentType.JSON)
                .body(acessorioCompativel(Fixtures.arvore(), List.of(Dados.ID_INEXISTENTE), Fixtures.PRODUTO_ATIVO))
                .when().post("/acessorios")
                .then().statusCode(400).body("errors.field", hasItem("idsCompativeis"));
    }

    @Test
    void acessorioNaoECompativelConsigoMesmo() {
        Fixtures.Arvore a = Fixtures.arvore();
        Map<String, Object> ac = acessorioCompativel(a, List.of(), Fixtures.PRODUTO_ATIVO);
        Map<String, Object> v = Fixtures.variante("AC-" + Dados.sufixo().toUpperCase(), 499.0, 3);
        ac.put("variantes", Fixtures.lista(v));
        io.restassured.path.json.JsonPath criado = given().contentType(ContentType.JSON).body(ac)
                .when().post("/acessorios").then().statusCode(200).extract().jsonPath();
        long id = criado.getLong("id");
        // ate a #9 o acessorio nao devolve variantes; depois, o PUT reaproveita a variante criada
        if (criado.getList("variantes") != null && !criado.getList("variantes").isEmpty())
            v.put("id", criado.getLong("variantes[0].id"));
        ac.put("idsCompativeis", List.of(id));
        given().contentType(ContentType.JSON).body(ac).when().put("/acessorios/{id}", id)
                .then().statusCode(400).body("errors.field", hasItem("idsCompativeis"));
    }

    @Test
    void vinculoEntreAcessoriosApareceNosDoisLados() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado helice = Fixtures.criarAcessorio(a, 129.0, 4);
        long idBateria = Fixtures.criar("/acessorios",
                acessorioCompativel(a, List.of(helice.id()), Fixtures.PRODUTO_ATIVO));

        given().when().get("/produtos/{id}", helice.id())
                .then().statusCode(200)
                .body("acessoriosCompativeis.id", hasItem((int) idBateria))
                .body("compativeis", hasSize(0));
        given().when().get("/produtos/{id}", idBateria)
                .then().statusCode(200)
                .body("compativeis.id", hasItem((int) helice.id()))
                .body("compativeis[0].tipo.nome", equalTo("ACESSORIO"));
    }

    @Test
    void listaVaziaDesfazOVinculo() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado drone = Fixtures.criarDrone(a, 7999.0, 2);
        Map<String, Object> ac = acessorioCompativel(a, List.of(drone.id()), Fixtures.PRODUTO_ATIVO);
        long id = Fixtures.criar("/acessorios", ac);

        ac.put("idsCompativeis", List.of());
        given().contentType(ContentType.JSON).body(ac).when().put("/acessorios/{id}", id)
                .then().statusCode(204);
        given().when().get("/produtos/{id}", id).then().statusCode(200).body("compativeis", hasSize(0));
        given().when().get("/produtos/{id}", drone.id())
                .then().statusCode(200).body("acessoriosCompativeis.id", not(hasItem((int) id)));
    }

    // compatibilidade nao e "uso": excluir o drone tira o vinculo, sem 409
    @Test
    void excluirProdutoCompativelTiraOVinculo() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado drone = Fixtures.criarDrone(a, 7999.0, 2);
        long id = Fixtures.criar("/acessorios",
                acessorioCompativel(a, List.of(drone.id()), Fixtures.PRODUTO_ATIVO));

        given().when().delete("/produtos/{id}", drone.id()).then().statusCode(204);
        given().when().get("/produtos/{id}", id).then().statusCode(200).body("compativeis", hasSize(0));
    }
}

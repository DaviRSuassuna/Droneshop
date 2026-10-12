package br.com.droneshop.catalogo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Categoria (entrega anterior às issues): CRUD em /categorias.
 * RASCUNHO aceita campos vazios; ATIVA/INATIVA exige nome e ordem de exibição.
 */
@QuarkusTest
@Tag("base")
class CategoriaResourceTest {

    static Map<String, Object> categoriaAtiva(String nome, int ordem) {
        Map<String, Object> c = new HashMap<>();
        c.put("nome", nome);
        c.put("ordemExibicao", ordem);
        c.put("descricao", "Categoria criada pelo teste.");
        c.put("status", "ATIVA");
        c.put("exibirMenu", true);
        c.put("iconeUrl", "https://cdn.exemplo.com.br/icone.svg");
        return c;
    }

    static long criar(Map<String, Object> dto) {
        return given().contentType(ContentType.JSON).body(dto)
                .when().post("/categorias")
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void criaCategoriaAtivaComSlugGerado() {
        String s = Dados.sufixo();
        given().contentType(ContentType.JSON).body(categoriaAtiva("Câmeras " + s, 5))
                .when().post("/categorias")
                .then().statusCode(200)
                .body("id", notNullValue())
                .body("slug", equalTo("cameras-" + s))
                .body("ordemExibicao", equalTo(5))
                .body("exibirMenu", equalTo(true));
    }

    @Test
    void rascunhoSemCamposRecebeOrdemZero() {
        given().contentType(ContentType.JSON).body(Map.of("status", "RASCUNHO"))
                .when().post("/categorias")
                .then().statusCode(200)
                .body("status", equalTo("RASCUNHO"))
                .body("ordemExibicao", equalTo(0));
    }

    @Test
    void rascunhoComCamposVaziosComoATelaEnviaEAceito() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("nome", "");
        dto.put("slug", "");
        dto.put("descricao", "");
        dto.put("status", "RASCUNHO");
        dto.put("exibirMenu", false);
        dto.put("iconeUrl", "");
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/categorias")
                .then().statusCode(200)
                .body("slug", startsWith("rascunho-"))
                .body("iconeUrl", nullValue());
    }

    @Test
    void ativaSemObrigatoriosResponde400() {
        given().contentType(ContentType.JSON).body(Map.of("status", "ATIVA"))
                .when().post("/categorias")
                .then().statusCode(400)
                .body("errors.field", hasItems("nome", "ordemExibicao"));
    }

    @Test
    void ordemNegativaResponde400() {
        given().contentType(ContentType.JSON).body(categoriaAtiva("Negativa " + Dados.sufixo(), -1))
                .when().post("/categorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("ordemExibicao"));
    }

    @Test
    void slugRepetidoResponde400() {
        String nome = "Repetida " + Dados.sufixo();
        criar(categoriaAtiva(nome, 1));
        given().contentType(ContentType.JSON).body(categoriaAtiva(nome, 2))
                .when().post("/categorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("slug"));
    }

    @Test
    void buscaPorNomeVemOrdenadaPelaOrdemDeExibicao() {
        String s = Dados.sufixo();
        criar(categoriaAtiva("Ordem B " + s, 20));
        criar(categoriaAtiva("Ordem A " + s, 10));

        List<Integer> ordens = given().when().get("/categorias/nome/{nome}", s)
                .then().statusCode(200)
                .body("$", hasSize(2))
                .extract().jsonPath().getList("ordemExibicao", Integer.class);
        org.hamcrest.MatcherAssert.assertThat(ordens.get(0), lessThan(ordens.get(1)));
    }

    @Test
    void buscaPorSlugEContagemPorNome() {
        String s = Dados.sufixo();
        long id = criar(categoriaAtiva("Slug " + s, 1));

        given().when().get("/categorias/slug/{slug}", "slug-" + s)
                .then().statusCode(200)
                .body("id", equalTo((int) id));

        given().when().get("/categorias/nome/{nome}/count", s)
                .then().statusCode(200)
                .body(equalTo("1"));
    }

    @Test
    void alteraEApagaCategoria() {
        String s = Dados.sufixo();
        long id = criar(categoriaAtiva("Antes " + s, 1));

        given().contentType(ContentType.JSON).body(categoriaAtiva("Depois " + s, 3))
                .when().put("/categorias/{id}", id)
                .then().statusCode(204);
        given().when().get("/categorias/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Depois " + s))
                .body("ordemExibicao", equalTo(3));

        given().when().delete("/categorias/{id}", id).then().statusCode(204);
        given().when().get("/categorias/nome/{nome}/count", s)
                .then().statusCode(200)
                .body(equalTo("0"));
    }

    @Test
    void apagarCategoriaComSubcategoriaResponde409() {
        String s = Dados.sufixo();
        long categoriaId = criar(categoriaAtiva("Com filha " + s, 1));
        given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Filha " + s, "categoriaId", categoriaId, "status", "ATIVA"))
                .when().post("/subcategorias")
                .then().statusCode(200);

        given().when().delete("/categorias/{id}", categoriaId)
                .then().statusCode(409)
                .contentType("application/problem+json")
                .body("detail", containsString("1 subcategoria"));
    }

    @Test
    void buscaPorIdOuSlugInexistenteResponde404() {
        given().when().get("/categorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().when().get("/categorias/slug/{slug}", "nao-existe-" + Dados.sufixo())
                .then().statusCode(404);
    }

    @Test
    void alterarInexistenteResponde404() {
        given().contentType(ContentType.JSON).body(categoriaAtiva("Fantasma " + Dados.sufixo(), 1))
                .when().put("/categorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }
}

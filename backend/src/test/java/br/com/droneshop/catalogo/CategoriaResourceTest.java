package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
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
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Categoria (entrega anterior às issues): CRUD em /categorias.
 * RASCUNHO aceita campos vazios; ATIVA/INATIVA exige nome, ordem de exibição e,
 * desde a #8, o tipo (a categoria organiza a vitrine dentro de drones ou de acessórios).
 */
@QuarkusTest
@Tag("base")
class CategoriaResourceTest {

    static Map<String, Object> categoriaAtiva(String nome, int ordem) {
        Map<String, Object> c = new HashMap<>();
        c.put("nome", nome);
        c.put("ordemExibicao", ordem);
        c.put("descricao", "Categoria criada pelo teste.");
        c.put("idStatus", Dados.ATIVA);
        c.put("idTipo", Fixtures.TIPO_DRONE);
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
                .body("status.nome", equalTo("Ativa"))
                .body("exibirMenu", equalTo(true));
    }

    @Test
    void rascunhoSemCamposRecebeOrdemZero() {
        given().contentType(ContentType.JSON).body(Map.of("idStatus", Dados.RASCUNHO))
                .when().post("/categorias")
                .then().statusCode(200)
                .body("status.nome", equalTo("Rascunho"))
                .body("ordemExibicao", equalTo(0));
    }

    @Test
    void rascunhoComCamposVaziosComoATelaEnviaEAceito() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("nome", "");
        dto.put("slug", "");
        dto.put("descricao", "");
        dto.put("idStatus", Dados.RASCUNHO);
        dto.put("exibirMenu", false);
        dto.put("iconeUrl", "");
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/categorias")
                .then().statusCode(200)
                .body("slug", startsWith("rascunho-"))
                .body("iconeUrl", nullValue());
    }

    @Test
    @Tag("issue-8")
    void tipoVoltaNaRespostaEAtivaSemTipoResponde400() {
        String s = Dados.sufixo();
        given().contentType(ContentType.JSON).body(categoriaAtiva("Baterias " + s, 1))
                .when().post("/categorias")
                .then().statusCode(200)
                .body("tipo.nome", equalTo("DRONE"));

        Map<String, Object> semTipo = categoriaAtiva("Sem tipo " + s, 1);
        semTipo.remove("idTipo");
        given().contentType(ContentType.JSON).body(semTipo)
                .when().post("/categorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("idTipo"));

        Map<String, Object> tipoInvalido = categoriaAtiva("Tipo invalido " + s, 1);
        tipoInvalido.put("idTipo", 99);
        given().contentType(ContentType.JSON).body(tipoInvalido)
                .when().post("/categorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("idTipo"));
    }

    @Test
    @Tag("issue-8")
    void naoMudaOTipoDeCategoriaComProduto() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.criarDrone(a, 5000.0, 1);
        Map<String, Object> c = categoriaAtiva("Categoria " + Dados.sufixo(), 1);
        c.put("idTipo", Fixtures.TIPO_ACESSORIO);
        given().contentType(ContentType.JSON).body(c)
                .when().put("/categorias/{id}", a.idCategoria())
                .then().statusCode(409)
                .body("detail", containsString("1 produto"));
    }

    @Test
    void ativaSemObrigatoriosResponde400() {
        given().contentType(ContentType.JSON).body(Map.of("idStatus", Dados.ATIVA))
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
    @SuppressWarnings("null") // getList: aviso só da análise de nulos do Eclipse
    void buscaPorNomeVemOrdenadaPelaOrdemDeExibicao() {
        String s = Dados.sufixo();
        criar(categoriaAtiva("Ordem B " + s, 20));
        criar(categoriaAtiva("Ordem A " + s, 10));

        var ordens = given().when().get("/categorias/nome/{nome}", s)
                .then().statusCode(200)
                .body("items", hasSize(2))
                .extract().jsonPath().getList("items.ordemExibicao", Integer.class);
        MatcherAssert.assertThat(ordens.get(0), lessThan(ordens.get(1)));
    }

    @Test
    void buscaPorSlugEPorNome() {
        String s = Dados.sufixo();
        long id = criar(categoriaAtiva("Slug " + s, 1));

        given().when().get("/categorias/slug/{slug}", "slug-" + s)
                .then().statusCode(200)
                .body("id", equalTo((int) id));

        given().when().get("/categorias/nome/{nome}", s)
                .then().statusCode(200)
                .body("totalItems", equalTo(1));
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
        given().when().get("/categorias/nome/{nome}", s)
                .then().statusCode(200)
                .body("totalItems", equalTo(0));
    }

    @Test
    void apagarCategoriaComSubcategoriaResponde409() {
        String s = Dados.sufixo();
        long idCategoria = criar(categoriaAtiva("Com filha " + s, 1));
        given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Filha " + s, "idCategoria", idCategoria, "idStatus", Dados.ATIVA))
                .when().post("/subcategorias")
                .then().statusCode(200);

        given().when().delete("/categorias/{id}", idCategoria)
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
    void alterarOuApagarInexistenteResponde404() {
        given().contentType(ContentType.JSON).body(categoriaAtiva("Fantasma " + Dados.sufixo(), 1))
                .when().put("/categorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().when().delete("/categorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }

    @Test
    void listaOsStatusDeCategoria() {
        given().when().get("/status-categoria")
                .then().statusCode(200)
                .body("nome", hasItems("Rascunho", "Ativa", "Inativa"));
    }
}

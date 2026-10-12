package br.com.droneshop.catalogo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Subcategoria (entrega anterior às issues): CRUD em /subcategorias.
 * Sempre ligada a uma Categoria (tabela separada, subcategoria.categoria_id).
 */
@QuarkusTest
@Tag("base")
class SubcategoriaResourceTest {

    private static Map<String, Object> subcategoria(String nome, long categoriaId) {
        Map<String, Object> s = new HashMap<>();
        s.put("nome", nome);
        s.put("categoriaId", categoriaId);
        s.put("ordemExibicao", 1);
        s.put("descricao", "Subcategoria criada pelo teste.");
        s.put("status", "ATIVA");
        return s;
    }

    private static long novaCategoria(String s) {
        return CategoriaResourceTest.criar(CategoriaResourceTest.categoriaAtiva("Mãe " + s, 1));
    }

    private static long criar(Map<String, Object> dto) {
        return given().contentType(ContentType.JSON).body(dto)
                .when().post("/subcategorias")
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void criaSubcategoriaComNomeDaCategoriaNaResposta() {
        String s = Dados.sufixo();
        long categoriaId = novaCategoria(s);
        given().contentType(ContentType.JSON).body(subcategoria("Hélices " + s, categoriaId))
                .when().post("/subcategorias")
                .then().statusCode(200)
                .body("slug", equalTo("helices-" + s))
                .body("categoriaId", equalTo((int) categoriaId))
                .body("categoriaNome", equalTo("Mãe " + s));
    }

    @Test
    void filtraPelaCategoria() {
        String s = Dados.sufixo();
        long categoriaId = novaCategoria(s);
        criar(subcategoria("Um " + s, categoriaId));
        criar(subcategoria("Dois " + s, categoriaId));

        given().queryParam("categoriaId", categoriaId)
                .when().get("/subcategorias")
                .then().statusCode(200)
                .body("$", hasSize(2))
                .body("categoriaId", everyItem(equalTo((int) categoriaId)));
    }

    @Test
    void semCategoriaResponde400() {
        Map<String, Object> dto = subcategoria("Órfã " + Dados.sufixo(), 1);
        dto.remove("categoriaId");
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("categoriaId"));
    }

    @Test
    void categoriaInexistenteResponde400() {
        given().contentType(ContentType.JSON).body(subcategoria("Perdida " + Dados.sufixo(), Dados.ID_INEXISTENTE))
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("categoriaId"));
    }

    @Test
    void nomeEmBrancoResponde400() {
        given().contentType(ContentType.JSON).body(subcategoria("  ", novaCategoria(Dados.sufixo())))
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("nome"));
    }

    @Test
    void statusDesconhecidoResponde400() {
        Map<String, Object> dto = subcategoria("Status " + Dados.sufixo(), novaCategoria(Dados.sufixo()));
        dto.put("status", "PUBLICADA");
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("status"));
    }

    @Test
    void slugRepetidoResponde400() {
        String s = Dados.sufixo();
        long categoriaId = novaCategoria(s);
        criar(subcategoria("Repetida " + s, categoriaId));
        given().contentType(ContentType.JSON).body(subcategoria("Repetida " + s, categoriaId))
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("slug"));
    }

    @Test
    void alteraMovendoParaOutraCategoriaEApaga() {
        String s = Dados.sufixo();
        long origem = novaCategoria(s);
        long destino = CategoriaResourceTest.criar(CategoriaResourceTest.categoriaAtiva("Destino " + s, 2));
        long id = criar(subcategoria("Movida " + s, origem));

        given().contentType(ContentType.JSON).body(subcategoria("Movida " + s, destino))
                .when().put("/subcategorias/{id}", id)
                .then().statusCode(204);
        given().when().get("/subcategorias/{id}", id)
                .then().statusCode(200)
                .body("categoriaId", equalTo((int) destino));

        given().when().delete("/subcategorias/{id}", id).then().statusCode(204);
        given().queryParam("categoriaId", destino)
                .when().get("/subcategorias")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void alterarInexistenteResponde404() {
        given().contentType(ContentType.JSON).body(subcategoria("Fantasma " + Dados.sufixo(), novaCategoria(Dados.sufixo())))
                .when().put("/subcategorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }

    @Test
    void buscaPorIdInexistenteResponde404() {
        given().when().get("/subcategorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }
}

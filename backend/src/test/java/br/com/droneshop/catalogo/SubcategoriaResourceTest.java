package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
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
 * Sempre ligada a uma Categoria (tabela separada, subcategoria.id_categoria).
 */
@QuarkusTest
@Tag("base")
class SubcategoriaResourceTest {

    static Map<String, Object> subcategoria(String nome, long idCategoria) {
        Map<String, Object> s = new HashMap<>();
        s.put("nome", nome);
        s.put("idCategoria", idCategoria);
        s.put("ordemExibicao", 1);
        s.put("descricao", "Subcategoria criada pelo teste.");
        s.put("idStatus", Dados.ATIVA);
        return s;
    }

    private static long novaCategoria(String s) {
        return CategoriaResourceTest.criar(CategoriaResourceTest.categoriaAtiva("Mãe " + s, 1));
    }

    static long criar(Map<String, Object> dto) {
        return given().contentType(ContentType.JSON).body(dto)
                .when().post("/subcategorias")
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void criaSubcategoriaComACategoriaAninhada() {
        String s = Dados.sufixo();
        long idCategoria = novaCategoria(s);
        given().contentType(ContentType.JSON).body(subcategoria("Hélices " + s, idCategoria))
                .when().post("/subcategorias")
                .then().statusCode(200)
                .body("slug", equalTo("helices-" + s))
                .body("categoria.id", equalTo((int) idCategoria))
                .body("categoria.nome", equalTo("Mãe " + s))
                .body("status.nome", equalTo("Ativa"));
    }

    @Test
    void filtraPelaCategoria() {
        String s = Dados.sufixo();
        long idCategoria = novaCategoria(s);
        criar(subcategoria("Um " + s, idCategoria));
        criar(subcategoria("Dois " + s, idCategoria));

        given().queryParam("idCategoria", idCategoria)
                .when().get("/subcategorias")
                .then().statusCode(200)
                .body("items", hasSize(2))
                .body("totalItems", equalTo(2))
                .body("items.categoria.id", everyItem(equalTo((int) idCategoria)));
    }

    @Test
    void buscaPorNome() {
        String s = Dados.sufixo();
        criar(subcategoria("Nomeada " + s, novaCategoria(s)));
        given().when().get("/subcategorias/nome/{nome}", s)
                .then().statusCode(200)
                .body("totalItems", equalTo(1));
    }

    @Test
    void semCategoriaResponde400() {
        Map<String, Object> dto = subcategoria("Órfã " + Dados.sufixo(), 1);
        dto.remove("idCategoria");
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("idCategoria"));
    }

    @Test
    void categoriaInexistenteResponde400() {
        given().contentType(ContentType.JSON).body(subcategoria("Perdida " + Dados.sufixo(), Dados.ID_INEXISTENTE))
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("idCategoria"));
    }

    @Test
    void nomeEmBrancoResponde400() {
        given().contentType(ContentType.JSON).body(subcategoria("  ", novaCategoria(Dados.sufixo())))
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("nome"));
    }

    @Test
    void statusInexistenteResponde400() {
        Map<String, Object> dto = subcategoria("Status " + Dados.sufixo(), novaCategoria(Dados.sufixo()));
        dto.put("idStatus", 99);
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/subcategorias")
                .then().statusCode(400)
                .body("errors.field", hasItem("idStatus"));
    }

    @Test
    void slugRepetidoResponde400() {
        String s = Dados.sufixo();
        long idCategoria = novaCategoria(s);
        criar(subcategoria("Repetida " + s, idCategoria));
        given().contentType(ContentType.JSON).body(subcategoria("Repetida " + s, idCategoria))
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
                .body("categoria.id", equalTo((int) destino));

        given().when().delete("/subcategorias/{id}", id).then().statusCode(204);
        given().queryParam("idCategoria", destino)
                .when().get("/subcategorias")
                .then().statusCode(200)
                .body("totalItems", equalTo(0));
    }

    @Test
    void inexistenteResponde404() {
        given().when().get("/subcategorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().contentType(ContentType.JSON).body(subcategoria("Fantasma " + Dados.sufixo(), novaCategoria(Dados.sufixo())))
                .when().put("/subcategorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().when().delete("/subcategorias/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }
}

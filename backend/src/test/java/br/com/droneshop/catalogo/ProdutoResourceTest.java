package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Produto: cadastro pelo /drones (D27) e listagem, consulta e exclusão em /produtos
 * com filtros combináveis. Era "base"; a #8 migrou para /drones. Desde a #9 o preço fica na variante.
 */
@QuarkusTest
@Tag("issue-8")
class ProdutoResourceTest {

    /** Marca, subcategoria e fornecedor próprios do teste. */
    record Arvore(long idMarca, long idCategoria, long idSubcategoria, long idFornecedor) {
    }

    static Arvore arvore(String s) {
        long idMarca = MarcaResourceTest.criar(MarcaResourceTest.marcaAtiva("Marca " + s));
        long idCategoria = CategoriaResourceTest.criar(CategoriaResourceTest.categoriaAtiva("Categoria " + s, 1));
        long idSubcategoria = SubcategoriaResourceTest.criar(SubcategoriaResourceTest.subcategoria("Sub " + s, idCategoria));
        long idFornecedor = FornecedorResourceTest.criar(FornecedorResourceTest.fornecedor(s, Dados.cnpjValido()));
        return new Arvore(idMarca, idCategoria, idSubcategoria, idFornecedor);
    }

    static Map<String, Object> produto(String nome, String modelo, Arvore a) {
        Map<String, Object> p = new HashMap<>();
        p.put("nome", nome);
        p.put("modelo", modelo);
        p.put("descricaoCurta", "Produto criado pelo teste.");
        p.put("idMarca", a.idMarca());
        p.put("idSubcategoria", a.idSubcategoria());
        p.put("idFornecedor", a.idFornecedor());
        p.put("idStatus", Dados.PRODUTO_ATIVO);
        p.put("destaque", false);
        p.put("pesoGramas", 895);
        p.put("variantes", Fixtures.lista(Fixtures.variante("PR-" + Dados.sufixo().toUpperCase(), 4999.90, 5)));
        return p;
    }

    static long criar(Map<String, Object> dto) {
        return given().contentType(ContentType.JSON).body(dto)
                .when().post("/drones")
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void criaProdutoComRelacionamentosAninhadosEModeloEmMaiusculas() {
        String s = Dados.sufixo();
        Arvore a = arvore(s);
        given().contentType(ContentType.JSON).body(produto("Hélice " + s, "hel-" + s, a))
                .when().post("/drones")
                .then().statusCode(200)
                .body("modelo", equalTo(("HEL-" + s).toUpperCase()))
                .body("marca.id", equalTo((int) a.idMarca()))
                .body("subcategoria.id", equalTo((int) a.idSubcategoria()))
                .body("subcategoria.categoria.id", equalTo((int) a.idCategoria()))
                .body("fornecedor.id", equalTo((int) a.idFornecedor()))
                .body("status.nome", equalTo("Ativo"));
    }

    @Test
    void filtrosCombinaveisDevolvemPagina() {
        String s = Dados.sufixo();
        Arvore a = arvore(s);
        criar(produto("Bateria " + s, "BAT-" + s, a));
        Map<String, Object> rascunho = produto("Hélice " + s, "HEL-" + s, a);
        rascunho.put("idStatus", Dados.RASCUNHO);
        criar(rascunho);

        given().queryParam("idMarca", a.idMarca())
                .when().get("/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(2))
                .body("items.marca.id", everyItem(equalTo((int) a.idMarca())));

        given().queryParam("idCategoria", a.idCategoria()).queryParam("idStatus", Dados.RASCUNHO)
                .when().get("/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].nome", equalTo("Hélice " + s));

        // nome ignora maiúsculas e acentos e também procura no modelo
        given().queryParam("nome", "HELICE " + s)
                .when().get("/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1));
        given().queryParam("nome", ("bat-" + s))
                .when().get("/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1));
    }

    @Test
    void modeloRepetidoResponde400() {
        String s = Dados.sufixo();
        Arvore a = arvore(s);
        criar(produto("Um " + s, "DUP-" + s, a));
        given().contentType(ContentType.JSON).body(produto("Dois " + s, "dup-" + s, a))
                .when().post("/drones")
                .then().statusCode(400)
                .body("errors.field", hasItem("modelo"));
    }

    @Test
    void referenciasInexistentesRespondem400() {
        Arvore a = new Arvore(Dados.ID_INEXISTENTE, 0, Dados.ID_INEXISTENTE, Dados.ID_INEXISTENTE);
        given().contentType(ContentType.JSON).body(produto("Órfão", "ORF-" + Dados.sufixo(), a))
                .when().post("/drones")
                .then().statusCode(400)
                .body("errors.field", hasItem("idMarca"));
    }

    @Test
    void obrigatoriosResponde400() {
        given().contentType(ContentType.JSON).body(Map.of())
                .when().post("/drones")
                .then().statusCode(400)
                .body("errors.field", hasItems("nome", "modelo", "idMarca", "idSubcategoria", "idFornecedor", "idStatus", "pesoGramas"));
    }

    @Test
    void precoNegativoResponde400() {
        String s = Dados.sufixo();
        Map<String, Object> dto = produto("Negativo " + s, "NEG-" + s, arvore(s));
        dto.put("variantes", Fixtures.lista(Fixtures.variante("NEG-" + s.toUpperCase(), -1, 1)));
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/drones")
                .then().statusCode(400)
                .body("errors.field", hasItem("variantes[0].preco"));
    }

    @Test
    void alteraEApaga() {
        String s = Dados.sufixo();
        Arvore a = arvore(s);
        long id = criar(produto("Antes " + s, "ALT-" + s, a));

        Map<String, Object> dto = produto("Depois " + s, "ALT-" + s, a);
        dto.put("variantes", Fixtures.lista(Fixtures.variante("ALT-" + s.toUpperCase(), 100, 5)));
        given().contentType(ContentType.JSON).body(dto)
                .when().put("/drones/{id}", id)
                .then().statusCode(204);
        given().when().get("/produtos/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Depois " + s))
                .body("variantes[0].preco", equalTo(100.0f));

        given().when().delete("/produtos/{id}", id).then().statusCode(204);
        given().when().get("/produtos/{id}", id).then().statusCode(404);
    }

    @Test
    void inexistenteResponde404() {
        String s = Dados.sufixo();
        given().contentType(ContentType.JSON).body(produto("Fantasma " + s, "FAN-" + s, arvore(s)))
                .when().put("/drones/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().when().delete("/produtos/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }

    @Test
    void listaOsStatusDeProduto() {
        given().when().get("/status-produto")
                .then().statusCode(200)
                .body("nome", hasItems("Rascunho", "Ativo", "Inativo"));
    }
}

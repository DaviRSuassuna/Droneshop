package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.containsString;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * O que está em uso não se exclui (409); a saída é inativar (D24).
 * Monta uma árvore própria marca + categoria > subcategoria + fornecedor > drone.
 */
@QuarkusTest
@Tag("issue-8")
class ExclusaoEmUsoTest {

    @Test
    void naoExcluiMarcaSubcategoriaNemFornecedorComProduto() {
        String s = Dados.sufixo();
        long idMarca = MarcaResourceTest.criar(MarcaResourceTest.marcaAtiva("Em uso " + s));
        long idCategoria = CategoriaResourceTest.criar(CategoriaResourceTest.categoriaAtiva("Em uso " + s, 1));
        long idSubcategoria = SubcategoriaResourceTest.criar(SubcategoriaResourceTest.subcategoria("Em uso " + s, idCategoria));
        long idFornecedor = FornecedorResourceTest.criar(FornecedorResourceTest.fornecedor(s, Dados.cnpjValido()));

        Map<String, Object> produto = new HashMap<>();
        produto.put("nome", "Drone " + s);
        produto.put("idMarca", idMarca);
        produto.put("idSubcategoria", idSubcategoria);
        produto.put("idFornecedor", idFornecedor);
        produto.put("idStatus", Dados.PRODUTO_ATIVO);
        produto.put("pesoGramas", 895);
        produto.putAll(Fixtures.dadosComerciais("EMUSO-" + s.toUpperCase(), 1999.90, 1));
        long idProduto = given().contentType(ContentType.JSON).body(produto)
                .when().post("/drones")
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");

        given().when().delete("/marcas/{id}", idMarca)
                .then().statusCode(409).body("detail", containsString("1 produto"));
        given().when().delete("/subcategorias/{id}", idSubcategoria)
                .then().statusCode(409).body("detail", containsString("1 produto"));
        given().when().delete("/fornecedores/{id}", idFornecedor)
                .then().statusCode(409).body("detail", containsString("1 produto"));

        // sem o produto, as três exclusões passam
        given().when().delete("/produtos/{id}", idProduto).then().statusCode(204);
        given().when().delete("/marcas/{id}", idMarca).then().statusCode(204);
        given().when().delete("/subcategorias/{id}", idSubcategoria).then().statusCode(204);
        given().when().delete("/fornecedores/{id}", idFornecedor).then().statusCode(204);
    }
}

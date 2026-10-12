package br.com.droneshop.catalogo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * O que está em uso não se exclui (409); a saída é inativar.
 * Monta uma árvore própria marca + categoria > subcategoria + fornecedor > produto.
 */
@QuarkusTest
@Tag("base")
class ExclusaoEmUsoTest {

    private static long post(String rota, Map<String, Object> dto) {
        return given().contentType(ContentType.JSON).body(dto)
                .when().post(rota)
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void naoExcluiMarcaSubcategoriaNemFornecedorComProduto() {
        String s = Dados.sufixo();

        Map<String, Object> marca = new HashMap<>();
        marca.put("nome", "Em uso " + s);
        marca.put("paisOrigem", "Brasil");
        marca.put("logoUrl", "https://cdn.exemplo.com.br/logo.png");
        marca.put("status", "ATIVA");
        long marcaId = post("/marcas", marca);

        long categoriaId = CategoriaResourceTest.criar(CategoriaResourceTest.categoriaAtiva("Em uso " + s, 1));
        long subcategoriaId = post("/subcategorias",
                Map.of("nome", "Em uso " + s, "categoriaId", categoriaId, "status", "ATIVA"));

        Map<String, Object> endereco = new HashMap<>();
        endereco.put("cep", "77001002");
        endereco.put("logradouro", "Quadra 104 Norte");
        endereco.put("numero", "12");
        endereco.put("bairro", "Plano Diretor Norte");
        endereco.put("idMunicipio", Dados.idMunicipio("TO", Dados.IBGE_PALMAS));
        Map<String, Object> fornecedor = new HashMap<>();
        fornecedor.put("razaoSocial", "Em uso " + s);
        fornecedor.put("cnpj", Dados.cnpjValido());
        fornecedor.put("nomeContato", "Contato " + s);
        fornecedor.put("emailContato", s + "@exemplo.com.br");
        fornecedor.put("endereco", endereco);
        fornecedor.put("status", "ATIVO");
        long fornecedorId = post("/fornecedores", fornecedor);

        Map<String, Object> produto = new HashMap<>();
        produto.put("nome", "Drone " + s);
        produto.put("modelo", "EMUSO-" + s);
        produto.put("preco", 1999.90);
        produto.put("marcaId", marcaId);
        produto.put("subcategoriaId", subcategoriaId);
        produto.put("fornecedorId", fornecedorId);
        produto.put("status", "ATIVO");
        long produtoId = post("/produtos", produto);

        given().when().delete("/marcas/{id}", marcaId)
                .then().statusCode(409).body("detail", containsString("1 produto"));
        given().when().delete("/subcategorias/{id}", subcategoriaId)
                .then().statusCode(409).body("detail", containsString("1 produto"));
        given().when().delete("/fornecedores/{id}", fornecedorId)
                .then().statusCode(409).body("detail", containsString("1 produto"));

        // sem o produto, as três exclusões passam
        given().when().delete("/produtos/{id}", produtoId).then().statusCode(204);
        given().when().delete("/marcas/{id}", marcaId).then().statusCode(204);
        given().when().delete("/subcategorias/{id}", subcategoriaId).then().statusCode(204);
        given().when().delete("/fornecedores/{id}", fornecedorId).then().statusCode(204);
    }
}

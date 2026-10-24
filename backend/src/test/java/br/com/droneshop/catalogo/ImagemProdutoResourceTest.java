package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Issue nova "Imagem principal do produto nas listagens e fotos na carga de dados".
 * Contrato: docs/issues/nova-imagem-produto.md. A etiqueta é o nome do arquivo.
 */
@QuarkusTest
@Tag("issue-imagem-produto")
class ImagemProdutoResourceTest {

    static final long IMAGEM = 1;

    /** PNG de 1x1 pixel. */
    static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

    /** WebP de 1x1 pixel. */
    static final byte[] WEBP = Base64.getDecoder().decode(
            "UklGRiQAAABXRUJQVlA4IBgAAAAwAQCdASoBAAEAAwA0JaQAA3AA/vuUAAA=");

    static long imagem(long idProduto, boolean principal) {
        return given().multiPart("idTipo", String.valueOf(IMAGEM))
                .multiPart("arquivo", "foto.png", PNG, "image/png")
                .multiPart("altText", "Produto de frente")
                .multiPart("principal", String.valueOf(principal))
                .when().post("/produtos/{id}/midias", idProduto)
                .then().statusCode(200).extract().jsonPath().getLong("id");
    }

    static String imagemNaListagem(Fixtures.ProdutoCriado p) {
        return given().queryParam("busca", p.sku()).queryParam("pageSize", 100)
                .when().get("/produtos").then().statusCode(200)
                .extract().jsonPath().getString("items.find { it.id == " + p.id() + " }.imagemPrincipal");
    }

    @Test
    void listagemEDetalheTrazemAImagemPrincipal() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        imagem(p.id(), true);
        assertTrue(imagemNaListagem(p).startsWith("/midias/arquivos/"));
        given().when().get("/produtos/{id}", p.id())
                .then().statusCode(200)
                .body("imagemPrincipal", startsWith("/midias/arquivos/"));
    }

    @Test
    void produtoSemImagemVemComNull() {
        Fixtures.ProdutoCriado p = Fixtures.criarAcessorio(100.0, 1);
        given().when().get("/produtos/{id}", p.id())
                .then().statusCode(200)
                .body("$", hasKey("imagemPrincipal"))
                .body("imagemPrincipal", nullValue());
    }

    @Test
    void trocarAPrincipalMudaAImagemDaListagem() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        imagem(p.id(), true);
        long segunda = imagem(p.id(), false);
        String antes = imagemNaListagem(p);
        given().contentType(ContentType.JSON).body(Map.of("principal", true))
                .when().patch("/midias/{id}", segunda).then().statusCode(204);
        String depois = imagemNaListagem(p);
        assertNotEquals(antes, depois);
        assertTrue(depois.startsWith("/midias/arquivos/"));
    }

    @Test
    void apagarAPrincipalDeixaNull() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        long id = imagem(p.id(), true);
        assertTrue(String.valueOf(imagemNaListagem(p)).startsWith("/midias/arquivos/"));
        given().when().delete("/midias/{id}", id).then().statusCode(204);
        assertNull(imagemNaListagem(p));
    }

    @Test
    void estoqueTrazAImagemDoProduto() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        imagem(p.id(), true);
        given().queryParam("busca", p.sku()).queryParam("pageSize", 100)
                .when().get("/estoque")
                .then().statusCode(200)
                .body("items.find { it.produto.id == " + p.id() + " }.produto.imagemPrincipal",
                        startsWith("/midias/arquivos/"));
    }

    @Test
    void uploadAceitaWebp() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        given().multiPart("idTipo", String.valueOf(IMAGEM))
                .multiPart("arquivo", "foto.webp", WEBP, "image/webp")
                .multiPart("altText", "Produto de frente")
                .multiPart("principal", "true")
                .when().post("/produtos/{id}/midias", p.id())
                .then().statusCode(200)
                .body("url", startsWith("/midias/arquivos/"));
    }

    @Test
    void cargaDeDadosTemFotoNoFalconENoNimbus() {
        for (String sku : new String[] { "DRN-FX2P-0042", "DRN-NM3-0038", "DRN-HSP-0015" }) {
            String url = given().queryParam("busca", sku).queryParam("pageSize", 10)
                    .when().get("/produtos").then().statusCode(200)
                    .body("items.find { it.sku == '" + sku + "' }.imagemPrincipal", notNullValue())
                    .extract().jsonPath().getString("items.find { it.sku == '" + sku + "' }.imagemPrincipal");
            anonimo().when().get(url).then().statusCode(200);
        }
    }

    @Test
    void versoesDoGrupoFalconTrazemFoto() {
        long idGrupo = given().queryParam("nome", "Falcon X2").queryParam("pageSize", 10)
                .when().get("/grupos-produto").then().statusCode(200)
                .extract().jsonPath().getLong("items.find { it.nome == 'Falcon X2' }.id");
        given().when().get("/grupos-produto/{id}", idGrupo)
                .then().statusCode(200)
                .body("versoes.imagemPrincipal", everyItem(startsWith("/midias/arquivos/")))
                .body("versoes.size()", equalTo(3));
    }
}

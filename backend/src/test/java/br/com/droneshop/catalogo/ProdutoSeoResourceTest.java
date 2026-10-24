package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #7 Produto: SEO, garantia e lançamento.
 * Contrato: docs/issues/007.md. Cria pelo /drones desde a #8 (D27); preço e
 * estoque vão no próprio produto desde a D40.
 */
@QuarkusTest
@Tag("issue-7")
class ProdutoSeoResourceTest {

    static Map<String, Object> produto(String nome) {
        Fixtures.Arvore a = Fixtures.arvore();
        Map<String, Object> p = ProdutoResourceTest.produto(nome, "SEO-" + Dados.sufixo(),
                new ProdutoResourceTest.Arvore(a.idMarca(), a.idCategoria(), a.idSubcategoria(), a.idFornecedor()));
        p.put("descricao", "Descrição longa do produto.");
        p.put("metaTitle", nome);
        p.put("metaDescription", "Meta description do " + nome);
        p.put("tags", "fpv,4k");
        p.put("garantiaMeses", 12);
        p.put("dataLancamento", "2026-03-01");
        return p;
    }

    static io.restassured.response.ValidatableResponse post(Map<String, Object> p) {
        return given().contentType(ContentType.JSON).body(p).when().post("/drones").then();
    }

    @Test
    void camposNovosVoltamNaResposta() {
        String s = Dados.sufixo();
        post(produto("Falcon " + s)).statusCode(200)
                .body("descricao", equalTo("Descrição longa do produto."))
                .body("metaTitle", equalTo("Falcon " + s))
                .body("tags", equalTo("fpv,4k"))
                .body("garantiaMeses", equalTo(12))
                .body("dataLancamento", equalTo("2026-03-01"));
    }

    @Test
    void slugGeradoDoNomeSemAcento() {
        String s = Dados.sufixo();
        Map<String, Object> p = produto("Águia Pró " + s);
        p.put("slug", "");
        post(p).statusCode(200).body("slug", equalTo("aguia-pro-" + s));
    }

    @Test
    void slugInformadoENormalizadoERepetidoResponde400() {
        String s = Dados.sufixo();
        Map<String, Object> p = produto("Produto " + s);
        p.put("slug", "Meu Slug " + s);
        post(p).statusCode(200).body("slug", equalTo("meu-slug-" + s));

        Map<String, Object> outro = produto("Outro " + s);
        outro.put("slug", "meu-slug-" + s);
        post(outro).statusCode(400).body("errors.field", hasItem("slug"));
    }

    @Test
    void buscaPorSlug() {
        String s = Dados.sufixo();
        long id = post(produto("Slug " + s)).statusCode(200).extract().jsonPath().getLong("id");
        given().when().get("/produtos/slug/{slug}", "slug-" + s)
                .then().statusCode(200).body("id", equalTo((int) id));
        given().when().get("/produtos/slug/{slug}", "nao-existe-" + s).then().statusCode(404);
    }

    @Test
    void garantiaVaziaHerdaAConfiguracaoDaLoja() {
        Map<String, Object> p = produto("Garantia " + Dados.sufixo());
        p.put("garantiaMeses", null);
        // carga: 365 dias -> 13 meses (arredonda para cima)
        post(p).statusCode(200).body("garantiaMeses", equalTo(13));
    }

    @Test
    void limitesDeSeoEGarantia() {
        Map<String, Object> p = produto("Limites " + Dados.sufixo());
        p.put("metaTitle", "x".repeat(71));
        p.put("metaDescription", "x".repeat(161));
        p.put("garantiaMeses", -1);
        post(p).statusCode(400)
                .body("errors.field", hasItem("metaTitle"))
                .body("errors.field", hasItem("metaDescription"))
                .body("errors.field", hasItem("garantiaMeses"));
    }

    @Test
    void rascunhoSalvaSoComOStatus() {
        post(Map.of("idStatus", Fixtures.PRODUTO_RASCUNHO)).statusCode(200)
                .body("id", notNullValue())
                .body("status.id", equalTo((int) Fixtures.PRODUTO_RASCUNHO));
    }
}

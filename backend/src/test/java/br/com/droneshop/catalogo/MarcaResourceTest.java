package br.com.droneshop.catalogo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Marca (entrega anterior às issues): CRUD em /marcas.
 * RASCUNHO aceita campos vazios; ATIVA/INATIVA exige nome, país de origem e logo.
 */
@QuarkusTest
@Tag("base")
class MarcaResourceTest {

    private static Map<String, Object> marcaAtiva(String nome) {
        Map<String, Object> m = new HashMap<>();
        m.put("nome", nome);
        m.put("paisOrigem", "Brasil");
        m.put("siteOficial", "https://exemplo.com.br");
        m.put("descricao", "Marca criada pelo teste.");
        m.put("logoUrl", "https://cdn.exemplo.com.br/logo.png");
        m.put("status", "ATIVA");
        m.put("destacarHome", true);
        return m;
    }

    private static long criar(Map<String, Object> dto) {
        return given().contentType(ContentType.JSON).body(dto)
                .when().post("/marcas")
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void criaMarcaAtivaEGeraSlugSemAcentoAPartirDoNome() {
        String s = Dados.sufixo();
        given().contentType(ContentType.JSON).body(marcaAtiva("Águia Céu " + s))
                .when().post("/marcas")
                .then().statusCode(200)
                .body("id", notNullValue())
                .body("slug", equalTo("aguia-ceu-" + s))
                .body("status", equalTo("ATIVA"))
                .body("destacarHome", equalTo(true));
    }

    @Test
    void usaOSlugInformadoNormalizado() {
        String s = Dados.sufixo();
        Map<String, Object> dto = marcaAtiva("Marca " + s);
        dto.put("slug", "Slug Próprio " + s);
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/marcas")
                .then().statusCode(200)
                .body("slug", equalTo("slug-proprio-" + s));
    }

    @Test
    void rascunhoSemNenhumCampoEAceitoComSlugTemporario() {
        given().contentType(ContentType.JSON).body(Map.of("status", "RASCUNHO"))
                .when().post("/marcas")
                .then().statusCode(200)
                .body("status", equalTo("RASCUNHO"))
                .body("slug", startsWith("rascunho-"));
    }

    @Test
    void ativaSemCamposObrigatoriosResponde400ComOsCampos() {
        given().contentType(ContentType.JSON).body(Map.of("status", "ATIVA"))
                .when().post("/marcas")
                .then().statusCode(400)
                .contentType("application/problem+json")
                .body("type", endsWith("/errors/validation-error"))
                .body("errors.field", hasItems("nome", "paisOrigem", "logoUrl"));
    }

    @Test
    void statusDesconhecidoResponde400() {
        Map<String, Object> dto = marcaAtiva("Marca " + Dados.sufixo());
        dto.put("status", "PUBLICADA");
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/marcas")
                .then().statusCode(400)
                .body("errors.field", hasItem("status"));
    }

    @Test
    void statusAusenteResponde400() {
        Map<String, Object> dto = marcaAtiva("Marca " + Dados.sufixo());
        dto.remove("status");
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/marcas")
                .then().statusCode(400)
                .body("errors.field", hasItem("status"));
    }

    @Test
    void nomeComUmCaractereResponde400() {
        given().contentType(ContentType.JSON).body(marcaAtiva("A"))
                .when().post("/marcas")
                .then().statusCode(400)
                .body("errors.field", hasItem("nome"));
    }

    @Test
    void slugRepetidoResponde400() {
        String nome = "Repetida " + Dados.sufixo();
        criar(marcaAtiva(nome));
        given().contentType(ContentType.JSON).body(marcaAtiva(nome))
                .when().post("/marcas")
                .then().statusCode(400)
                .body("errors.field", hasItem("slug"));
    }

    @Test
    void buscaPorIdESlug() {
        String s = Dados.sufixo();
        long id = criar(marcaAtiva("Busca " + s));

        given().when().get("/marcas/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Busca " + s));

        given().when().get("/marcas/slug/{slug}", "busca-" + s)
                .then().statusCode(200)
                .body("id", equalTo((int) id));
    }

    @Test
    void buscaPorNomeEContagem() {
        String s = Dados.sufixo();
        criar(marcaAtiva("Contada " + s));

        given().when().get("/marcas/nome/{nome}", s)
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].nome", equalTo("Contada " + s));

        given().when().get("/marcas/nome/{nome}/count", s)
                .then().statusCode(200)
                .body(equalTo("1"));
    }

    @Test
    void listagemRespeitaOTamanhoDaPagina() {
        given().queryParam("page", 0).queryParam("pageSize", 2)
                .when().get("/marcas")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    void alteraMarca() {
        String s = Dados.sufixo();
        long id = criar(marcaAtiva("Antes " + s));

        Map<String, Object> dto = marcaAtiva("Depois " + s);
        dto.put("status", "INATIVA");
        given().contentType(ContentType.JSON).body(dto)
                .when().put("/marcas/{id}", id)
                .then().statusCode(204);

        given().when().get("/marcas/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Depois " + s))
                .body("slug", equalTo("depois-" + s))
                .body("status", equalTo("INATIVA"));
    }

    @Test
    void alterarParaOSlugDeOutraMarcaResponde400() {
        String s = Dados.sufixo();
        criar(marcaAtiva("Primeira " + s));
        long id = criar(marcaAtiva("Segunda " + s));

        given().contentType(ContentType.JSON).body(marcaAtiva("Primeira " + s))
                .when().put("/marcas/{id}", id)
                .then().statusCode(400)
                .body("errors.field", hasItem("slug"));
    }

    @Test
    void apagaMarca() {
        String s = Dados.sufixo();
        long id = criar(marcaAtiva("Apagada " + s));

        given().when().delete("/marcas/{id}", id).then().statusCode(204);

        given().when().get("/marcas/nome/{nome}/count", s)
                .then().statusCode(200)
                .body(equalTo("0"));
    }

    @Test
    void buscaPorIdOuSlugInexistenteResponde404() {
        given().when().get("/marcas/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404)
                .contentType("application/problem+json")
                .body("type", endsWith("/errors/not-found"));
        given().when().get("/marcas/slug/{slug}", "nao-existe-" + Dados.sufixo())
                .then().statusCode(404);
    }

    @Test
    void apagarInexistenteResponde404() {
        given().when().delete("/marcas/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }

    @Test
    void alterarInexistenteResponde404() {
        given().contentType(ContentType.JSON).body(marcaAtiva("Fantasma " + Dados.sufixo()))
                .when().put("/marcas/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }

    @Test
    void buscaPorNomeIgnoraMaiusculas() {
        String s = Dados.sufixo();
        criar(marcaAtiva("Maiuscula " + s));
        given().when().get("/marcas/nome/{nome}", "MAIUSCULA " + s.toUpperCase())
                .then().statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    void rascunhoComCamposVaziosComoATelaEnviaEAceito() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("nome", "");
        dto.put("slug", "");
        dto.put("paisOrigem", "");
        dto.put("siteOficial", "");
        dto.put("descricao", "");
        dto.put("logoUrl", "");
        dto.put("status", "RASCUNHO");
        dto.put("destacarHome", false);
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/marcas")
                .then().statusCode(200)
                .body("slug", startsWith("rascunho-"))
                .body("siteOficial", nullValue());
    }
}

package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #12 MidiaProduto: imagem e manual por upload, vídeo por URL (D29, RNF08).
 * Contrato: docs/issues/012.md
 */
@QuarkusTest
@Tag("issue-12")
class MidiaProdutoResourceTest {

    static final long IMAGEM = 1, VIDEO = 2, MANUAL_PDF = 3;

    /** PNG de 1x1 pixel. */
    static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
    static final byte[] PDF = "%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF".getBytes();

    static ValidatableResponse imagem(long idProduto, String alt, boolean principal) {
        return given().multiPart("idTipo", String.valueOf(IMAGEM))
                .multiPart("arquivo", "foto.png", PNG, "image/png")
                .multiPart("altText", alt)
                .multiPart("principal", String.valueOf(principal))
                .when().post("/produtos/{id}/midias", idProduto).then();
    }

    @Test
    void listaOsTiposDeMidia() {
        given().when().get("/tipos-midia").then().statusCode(200)
                .body("nome", contains("IMAGEM", "VIDEO", "MANUAL_PDF"));
    }

    @Test
    void sobeImagemEServeOArquivo() {
        long id = Fixtures.criarDrone(1000.0, 1).id();
        String url = imagem(id, "Drone de frente", true).statusCode(200)
                .body("tipo.nome", equalTo("IMAGEM"))
                .body("principal", equalTo(true))
                .body("altText", equalTo("Drone de frente"))
                .body("url", startsWith("/midias/arquivos/"))
                .extract().jsonPath().getString("url");
        anonimo().when().get(url).then().statusCode(200).contentType("image/png");
    }

    @Test
    void imagemSemTextoAlternativoResponde400() {
        long id = Fixtures.criarDrone(1000.0, 1).id();
        imagem(id, "", false).statusCode(400).body("errors.field", hasItem("altText"));
    }

    @Test
    void arquivoDiferenteDoTipoResponde400() {
        long id = Fixtures.criarDrone(1000.0, 1).id();
        given().multiPart("idTipo", String.valueOf(IMAGEM))
                .multiPart("arquivo", "manual.pdf", PDF, "application/pdf")
                .multiPart("altText", "x")
                .when().post("/produtos/{id}/midias", id)
                .then().statusCode(400).body("errors.field", hasItem("arquivo"));
    }

    @Test
    void extensaoDaImagemDeveCorresponderAoArquivo() {
        long id = Fixtures.criarDrone(1000.0, 1).id();
        given().multiPart("idTipo", String.valueOf(IMAGEM))
                .multiPart("arquivo", "foto.jpg", PNG, "image/jpeg")
                .multiPart("altText", "Drone de frente")
                .when().post("/produtos/{id}/midias", id)
                .then().statusCode(400).body("errors.field", hasItem("arquivo"));
    }

    @Test
    void arquivoMaiorQue20MbResponde400() {
        long id = Fixtures.criarDrone(1000.0, 1).id();
        byte[] acimaDoLimite = new byte[20 * 1024 * 1024 + 1];
        given().multiPart("idTipo", String.valueOf(IMAGEM))
                .multiPart("arquivo", "foto.png", acimaDoLimite, "image/png")
                .multiPart("altText", "Imagem grande")
                .when().post("/produtos/{id}/midias", id)
                .then().statusCode(400).body("errors.field", hasItem("arquivo"));
    }

    @Test
    void sobeManualEVideoPorUrl() {
        long id = Fixtures.criarDrone(1000.0, 1).id();
        given().multiPart("idTipo", String.valueOf(MANUAL_PDF))
                .multiPart("arquivo", "manual.pdf", PDF, "application/pdf")
                .when().post("/produtos/{id}/midias", id)
                .then().statusCode(200).body("tipo.nome", equalTo("MANUAL_PDF"));
        given().multiPart("idTipo", String.valueOf(VIDEO))
                .multiPart("url", "https://www.youtube.com/watch?v=abc")
                .when().post("/produtos/{id}/midias", id)
                .then().statusCode(200).body("url", equalTo("https://www.youtube.com/watch?v=abc"));
        given().multiPart("idTipo", String.valueOf(VIDEO))
                .when().post("/produtos/{id}/midias", id)
                .then().statusCode(400).body("errors.field", hasItem("url"));
    }

    @Test
    void soUmaPrincipalPorProduto() {
        long id = Fixtures.criarDrone(1000.0, 1).id();
        imagem(id, "Primeira", true).statusCode(200);
        long segunda = imagem(id, "Segunda", false).statusCode(200).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON).body(Map.of("principal", true))
                .when().patch("/midias/{id}", segunda).then().statusCode(204);
        given().when().get("/produtos/{id}/midias", id)
                .then().statusCode(200)
                .body("findAll { it.principal }", hasSize(1))
                .body("find { it.principal }.altText", equalTo("Segunda"));
    }

    @Test
    void alteraOrdemETextoAlternativo() {
        long idProduto = Fixtures.criarDrone(1000.0, 1).id();
        long idMidia = imagem(idProduto, "Frente", true).statusCode(200).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON).body(Map.of("ordem", 4))
                .when().patch("/midias/{id}", idMidia).then().statusCode(204);
        given().contentType(ContentType.JSON).body(Map.of("altText", "Vista frontal"))
                .when().patch("/midias/{id}", idMidia).then().statusCode(204);
        given().when().get("/produtos/{id}/midias", idProduto).then().statusCode(200)
                .body("[0].ordem", equalTo(4))
                .body("[0].altText", equalTo("Vista frontal"));
    }

    @Test
    void reordenaEExclui() {
        long id = Fixtures.criarDrone(1000.0, 1).id();
        var primeiroUpload = imagem(id, "A", true).statusCode(200);
        long a = primeiroUpload.extract().jsonPath().getLong("id");
        String urlArquivo = primeiroUpload.extract().jsonPath().getString("url");
        long b = imagem(id, "B", false).statusCode(200).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of(b, a)))
                .when().patch("/produtos/{id}/midias/ordem", id).then().statusCode(204);
        given().when().get("/produtos/{id}/midias", id)
                .then().statusCode(200).body("altText", contains("B", "A"));
        given().when().delete("/midias/{id}", a).then().statusCode(204);
        anonimo().when().get(urlArquivo).then().statusCode(404);
        given().when().get("/produtos/{id}/midias", id).then().statusCode(200).body("$", hasSize(1));
    }

    @Test
    void produtoInexistenteResponde404() {
        imagem(Dados.ID_INEXISTENTE, "x", false).statusCode(404);
    }
}

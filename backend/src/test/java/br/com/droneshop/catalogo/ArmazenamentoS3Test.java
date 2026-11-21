package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.ArmazenamentoMemoria;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;

/**
 * #95 "Mídias do produto no armazenamento S3 (RustFS)". Contrato e regras da D42 em
 * docs/issues/nova-armazenamento-s3.md. O S3 é o falso em memória (testes/ArmazenamentoMemoria).
 */
@QuarkusTest
@Tag("issue-armazenamento-s3")
class ArmazenamentoS3Test {

    static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
    static final byte[] PDF = "%PDF-1.4\n%%EOF\n".getBytes();

    @Inject
    ArmazenamentoMemoria armazenamento;

    @AfterEach
    void voltaAoAr() {
        armazenamento.foraDoAr(false);
    }

    static ValidatableResponse enviar(long idProduto, String nome, byte[] conteudo, String tipo, boolean principal) {
        return given().multiPart("idTipo", "1")
                .multiPart("arquivo", nome, conteudo, tipo)
                .multiPart("altText", "Foto " + nome)
                .multiPart("principal", String.valueOf(principal))
                .when().post("/produtos/{id}/midias", idProduto).then();
    }

    static String nome(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    static void inativar(long id) {
        Map<String, Object> corpo = Fixtures.corpoDoProduto(id);
        corpo.put("idStatus", Fixtures.PRODUTO_INATIVO);
        Fixtures.alterarProduto(id, corpo).then().statusCode(204);
    }

    @Test
    void uploadGravaNoBucketComChaveDoProduto() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        String url = enviar(p.id(), "frente.png", PNG, "image/png", true).statusCode(200)
                .body("url", startsWith("/midias/arquivos/")).extract().jsonPath().getString("url");
        assertTrue(armazenamento.contem("produtos/" + p.id() + "/" + nome(url)));
        byte[] lido = anonimo().when().get(url).then().statusCode(200)
                .header("Content-Type", startsWith("image/png"))
                .header("Content-Disposition", startsWith("inline"))
                .extract().asByteArray();
        assertArrayEquals(PNG, lido);
    }

    @Test
    void downloadVemComoAnexo() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        String url = enviar(p.id(), "frente.png", PNG, "image/png", true).statusCode(200)
                .extract().jsonPath().getString("url");
        anonimo().queryParam("download", true).when().get(url).then().statusCode(200)
                .header("Content-Disposition", startsWith("attachment"));
    }

    @Test
    void imagemAcimaDe5MbVolta400() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        byte[] grande = Arrays.copyOf(PNG, 5 * 1024 * 1024 + 1);
        enviar(p.id(), "grande.png", grande, "image/png", true).statusCode(400)
                .body("errors.field", hasItem("arquivo"));
    }

    @Test
    void assinaturaQueNaoBateVolta400() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        enviar(p.id(), "falso.png", PDF, "image/png", true).statusCode(400)
                .body("errors.field", hasItem("arquivo"));
        enviar(p.id(), "foto.png", PNG, "application/pdf", true).statusCode(400)
                .body("errors.field", hasItem("arquivo"));
    }

    @Test
    void manualPdfAte20Mb() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        given().multiPart("idTipo", "3").multiPart("arquivo", "manual.pdf", PDF, "application/pdf")
                .when().post("/produtos/{id}/midias", p.id()).then().statusCode(200);
        byte[] grande = Arrays.copyOf(PDF, 20 * 1024 * 1024 + 1);
        given().multiPart("idTipo", "3").multiPart("arquivo", "grande.pdf", grande, "application/pdf")
                .when().post("/produtos/{id}/midias", p.id()).then().statusCode(400)
                .body("errors.field", hasItem("arquivo"));
    }

    @Test
    void excluirRemoveOObjetoEPromoveAProxima() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        var principal = enviar(p.id(), "a.png", PNG, "image/png", true).statusCode(200).extract().jsonPath();
        long segunda = enviar(p.id(), "b.png", PNG, "image/png", false).statusCode(200)
                .extract().jsonPath().getLong("id");
        given().when().delete("/midias/{id}", principal.getLong("id")).then().statusCode(204);
        assertFalse(armazenamento.contemArquivo(nome(principal.getString("url"))));
        given().when().get("/produtos/{id}/midias", p.id()).then().statusCode(200)
                .body("find { it.id == " + segunda + " }.principal", equalTo(true));
    }

    @Test
    void ultimaImagemDeProdutoAtivoNaoSeExclui() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        long id = enviar(p.id(), "a.png", PNG, "image/png", true).statusCode(200)
                .extract().jsonPath().getLong("id");
        given().when().delete("/midias/{id}", id).then().statusCode(409);
        inativar(p.id());
        given().when().delete("/midias/{id}", id).then().statusCode(204);
    }

    @Test
    void excluirOProdutoRemoveTodosOsObjetos() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        String a = enviar(p.id(), "a.png", PNG, "image/png", true).statusCode(200).extract().jsonPath().getString("url");
        String b = enviar(p.id(), "b.png", PNG, "image/png", false).statusCode(200).extract().jsonPath().getString("url");
        given().when().delete("/produtos/{id}", p.id()).then().statusCode(204);
        assertFalse(armazenamento.contemArquivo(nome(a)));
        assertFalse(armazenamento.contemArquivo(nome(b)));
    }

    @Test
    void armazenamentoForaDoArVolta503() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 1);
        armazenamento.foraDoAr(true);
        enviar(p.id(), "a.png", PNG, "image/png", true).statusCode(503);
    }

    @Test
    void cargaDeDadosFoiParaOBucket() {
        assertTrue(armazenamento.contemArquivo("3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d01.webp"));
    }
}

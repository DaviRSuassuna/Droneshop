package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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

/**
 * Issue nova "Logo da marca por upload, peso do acessório, sugestão nos compatíveis e kit em Ofertas".
 * Contrato: docs/issues/nova-catalogo-complementos.md. A etiqueta é o nome do arquivo.
 * Os dois testes da loja dependem também da #21.
 */
@QuarkusTest
@Tag("issue-catalogo-complementos")
class CatalogoComplementosResourceTest {

    /** PNG de 1x1 pixel. */
    static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
    static final byte[] PDF = "%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF".getBytes();

    static long acessorio(Fixtures.Arvore a, Map<String, Object> extras) {
        String s = Dados.sufixo();
        Map<String, Object> corpo = Fixtures.acessorio("Acessorio " + s, a,
                Fixtures.dadosComerciais("AC-" + s.toUpperCase(), 300.0, 5));
        corpo.putAll(extras);
        return Fixtures.criar("/acessorios", corpo);
    }

    @Test
    void logoDaMarcaPorUploadVaiParaOLogoUrl() {
        long idMarca = Fixtures.arvore().idMarca();
        String url = given().multiPart("arquivo", "logo.png", PNG, "image/png")
                .when().post("/marcas/{id}/logo", idMarca)
                .then().statusCode(200)
                .body("logoUrl", startsWith("/midias/arquivos/"))
                .extract().jsonPath().getString("logoUrl");
        given().when().get("/marcas/{id}", idMarca)
                .then().statusCode(200)
                .body("logoUrl", equalTo(url));
        anonimo().when().get(url).then().statusCode(200);
    }

    @Test
    void logoSoAceitaPngOuSvg() {
        long idMarca = Fixtures.arvore().idMarca();
        given().multiPart("arquivo", "logo.pdf", PDF, "application/pdf")
                .when().post("/marcas/{id}/logo", idMarca)
                .then().statusCode(400)
                .body("errors.field", hasItem("arquivo"));
        given().multiPart("arquivo", "logo.png", PNG, "image/png")
                .when().post("/marcas/{id}/logo", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }

    @Test
    void acessorioGuardaPesoDoItemESugestao() {
        long id = acessorio(Fixtures.arvore(), Map.of("pesoItemG", 85, "sugerirNosCompativeis", true));
        given().when().get("/acessorios/{id}", id)
                .then().statusCode(200)
                .body("pesoItemG", equalTo(85))
                .body("sugerirNosCompativeis", equalTo(true));
    }

    @Test
    void semSugestaoInformadaValeFalseEPesoNegativoDa400() {
        Fixtures.Arvore a = Fixtures.arvore();
        long id = acessorio(a, Map.of());
        given().when().get("/acessorios/{id}", id)
                .then().statusCode(200)
                .body("sugerirNosCompativeis", equalTo(false));

        String s = Dados.sufixo();
        Map<String, Object> corpo = Fixtures.acessorio("Acessorio " + s, a,
                Fixtures.dadosComerciais("AC-" + s.toUpperCase(), 300.0, 5));
        corpo.put("pesoItemG", -1);
        given().contentType(ContentType.JSON).body(corpo)
                .when().post("/acessorios")
                .then().statusCode(400)
                .body("errors.field", hasItem("pesoItemG"));
    }

    @Test
    void kitGuardaExibirEmOfertas() {
        long componente = Fixtures.criarAcessorio(300.0, 5).id();
        Map<String, Object> corpo = Fixtures.kit("Kit " + Dados.sufixo(), Fixtures.arvore(),
                Fixtures.lista(Fixtures.componente(componente, 2)));
        corpo.put("exibirEmOfertas", true);
        long id = Fixtures.criar("/kits", corpo);
        given().when().get("/kits/{id}", id)
                .then().statusCode(200)
                .body("exibirEmOfertas", equalTo(true));
    }

    @Test
    void paginaDoDroneSugereSoOsAcessoriosMarcados() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado drone = Fixtures.criarDrone(a, 5000.0, 3);
        long sugerido = acessorio(a, Map.of("idsCompativeis", List.of(drone.id()), "sugerirNosCompativeis", true));
        long naoSugerido = acessorio(a, Map.of("idsCompativeis", List.of(drone.id()), "sugerirNosCompativeis", false));
        anonimo().when().get("/loja/produtos/{slug}/compativeis", drone.slug())
                .then().statusCode(200)
                .body("id", hasItem((int) sugerido))
                .body("id", not(hasItem((int) naoSugerido)));
    }

    @Test
    void kitMarcadoApareceEmOfertasComPrecoDosComponentes() {
        long componente = Fixtures.criarAcessorio(300.0, 5).id();
        Map<String, Object> corpo = Fixtures.kit("Kit " + Dados.sufixo(), Fixtures.arvore(),
                Fixtures.lista(Fixtures.componente(componente, 2)));
        corpo.put("preco", 550.0);
        corpo.put("custoAquisicao", 360.0);
        corpo.put("exibirEmOfertas", true);
        long id = Fixtures.criar("/kits", corpo);
        anonimo().when().get("/loja/ofertas")
                .then().statusCode(200)
                .body("find { it.produto.id == " + id + " }.precoDe", equalTo(600.0f))
                .body("find { it.produto.id == " + id + " }.precoPor", equalTo(550.0f));
    }
}

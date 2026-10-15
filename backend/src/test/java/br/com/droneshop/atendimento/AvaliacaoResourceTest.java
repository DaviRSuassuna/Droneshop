package br.com.droneshop.atendimento;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #39 Avaliações com compra verificada e moderação (RF35, RF36, RN04, RN05).
 * Contrato: docs/issues/039.md
 */
@QuarkusTest
@Tag("issue-39")
class AvaliacaoResourceTest {

    static ValidatableResponse avaliar(String token, String slug, int nota) {
        return como(token).contentType(ContentType.JSON).body(Map.of("nota", nota, "comentario", "Muito bom"))
                .when().post("/loja/produtos/{slug}/avaliacoes", slug).then();
    }

    static void moderar(long id, long idStatus) {
        given().contentType(ContentType.JSON).body(Map.of("idStatus", idStatus))
                .when().patch("/admin/avaliacoes/{id}", id).then().statusCode(204);
    }

    @Test
    void listaStatusDeModeracao() {
        anonimo().when().get("/status-moderacao").then().statusCode(200)
                .body("nome", contains("PENDENTE", "APROVADA", "REJEITADA"));
    }

    @Test
    void soPublicaDepoisDeAprovada() {
        Fixtures.ProdutoCriado p = Fixtures.criarAcessorio(100.0, 5);
        Fixtures.Cliente c = Fixtures.novoCliente();
        long id = avaliar(c.token(), p.slug(), 5).statusCode(200)
                .body("status.nome", equalTo("PENDENTE"))
                .extract().jsonPath().getLong("id");
        anonimo().when().get("/loja/produtos/{slug}/avaliacoes", p.slug())
                .then().statusCode(200).body("quantidade", equalTo(0)).body("itens", hasSize(0));
        moderar(id, Fixtures.MODERACAO_APROVADA);
        anonimo().when().get("/loja/produtos/{slug}/avaliacoes", p.slug())
                .then().statusCode(200).body("quantidade", equalTo(1)).body("media", equalTo(5.0f));
    }

    @Test
    void compraVerificadaSoComItemEntregue() {
        Fixtures.ProdutoCriado p = Fixtures.criarAcessorio(100.0, 5);
        Fixtures.Cliente comprou = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPago(comprou, p.idVariante(), 1);
        Fixtures.avancarAteEntregue(pedido);
        avaliar(comprou.token(), p.slug(), 4).statusCode(200).body("compraVerificada", equalTo(true));

        Fixtures.Cliente naoComprou = Fixtures.novoCliente();
        avaliar(naoComprou.token(), p.slug(), 4).statusCode(200).body("compraVerificada", equalTo(false));

        Fixtures.Cliente naoRecebeu = Fixtures.novoCliente();
        Fixtures.pedidoPago(naoRecebeu, p.idVariante(), 1);
        avaliar(naoRecebeu.token(), p.slug(), 4).statusCode(200).body("compraVerificada", equalTo(false));
    }

    @Test
    void notaForaDaFaixaEDuplicada() {
        Fixtures.ProdutoCriado p = Fixtures.criarAcessorio(100.0, 5);
        Fixtures.Cliente c = Fixtures.novoCliente();
        avaliar(c.token(), p.slug(), 6).statusCode(400).body("errors.field", hasItem("nota"));
        avaliar(c.token(), p.slug(), 0).statusCode(400).body("errors.field", hasItem("nota"));
        avaliar(c.token(), p.slug(), 3).statusCode(200);
        avaliar(c.token(), p.slug(), 4).statusCode(409);
    }
}

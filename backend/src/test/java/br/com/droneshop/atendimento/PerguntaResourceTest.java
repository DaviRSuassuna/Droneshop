package br.com.droneshop.atendimento;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
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
 * #40 Perguntas e respostas sobre produtos (RF36, RF37, RN05).
 * Contrato: docs/issues/040.md
 */
@QuarkusTest
@Tag("issue-40")
class PerguntaResourceTest {

    static ValidatableResponse perguntar(String token, String slug, String texto) {
        return como(token).contentType(ContentType.JSON).body(Map.of("texto", texto))
                .when().post("/loja/produtos/{slug}/perguntas", slug).then();
    }

    static void moderar(long id, long idStatus) {
        given().contentType(ContentType.JSON).body(Map.of("idStatus", idStatus))
                .when().patch("/admin/perguntas/{id}", id).then().statusCode(204);
    }

    @Test
    void perguntaSoApareceDepoisDeAprovada() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        Fixtures.Cliente c = Fixtures.novoCliente();
        long id = perguntar(c.token(), p.slug(), "Funciona com óculos FPV?").statusCode(200)
                .body("status.nome", equalTo("PENDENTE")).extract().jsonPath().getLong("id");
        anonimo().when().get("/loja/produtos/{slug}/perguntas", p.slug()).then().statusCode(200).body("$", hasSize(0));
        moderar(id, Fixtures.MODERACAO_APROVADA);
        anonimo().when().get("/loja/produtos/{slug}/perguntas", p.slug()).then().statusCode(200).body("$", hasSize(1));
    }

    @Test
    void respostaDaLojaEOficialEDeClienteNao() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        Fixtures.Cliente c = Fixtures.novoCliente();
        long id = perguntar(c.token(), p.slug(), "Qual a autonomia real?").statusCode(200)
                .extract().jsonPath().getLong("id");
        moderar(id, Fixtures.MODERACAO_APROVADA);
        given().contentType(ContentType.JSON).body(Map.of("texto", "Cerca de 40 minutos."))
                .when().post("/admin/perguntas/{id}/respostas", id)
                .then().statusCode(200).body("oficial", equalTo(true));
        Fixtures.Cliente outro = Fixtures.novoCliente();
        como(outro.token()).contentType(ContentType.JSON).body(Map.of("texto", "No meu deu 38."))
                .when().post("/loja/perguntas/{id}/respostas", id)
                .then().statusCode(200).body("oficial", equalTo(false));
        anonimo().when().get("/loja/produtos/{slug}/perguntas", p.slug())
                .then().statusCode(200).body("[0].respostas", hasSize(2));
    }

    @Test
    void naoRespondePerguntaPendente() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        Fixtures.Cliente c = Fixtures.novoCliente();
        long id = perguntar(c.token(), p.slug(), "Tem garantia nacional?").statusCode(200)
                .extract().jsonPath().getLong("id");
        como(c.token()).contentType(ContentType.JSON).body(Map.of("texto", "Respondendo cedo demais"))
                .when().post("/loja/perguntas/{id}/respostas", id).then().statusCode(409);
    }

    @Test
    void textoCurtoResponde400() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(3000.0, 2);
        perguntar(Fixtures.novoCliente().token(), p.slug(), "Oi?").statusCode(400)
                .body("errors.field", hasItem("texto"));
    }
}

package br.com.droneshop.atendimento;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #38 Chamados de suporte com histórico de mensagens (RF33, RF34).
 * Contrato: docs/issues/038.md
 */
@QuarkusTest
@Tag("issue-38")
class ChamadoResourceTest {

    static final long ENCERRADO = 3;

    static ValidatableResponse abrir(String token, Long idPedido) {
        Map<String, Object> c = new HashMap<>();
        c.put("assunto", "Drone não liga");
        c.put("idPedido", idPedido);
        c.put("mensagem", "Comprei e não liga.");
        return como(token).contentType(ContentType.JSON).body(c).when().post("/chamados").then();
    }

    @Test
    void abreChamadoComProtocoloLegivel() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        String p1 = abrir(c.token(), null).statusCode(200)
                .body("protocolo", matchesPattern("AER-\\d{8}-\\d{4}"))
                .body("status.nome", equalTo("ABERTO"))
                .body("mensagens", hasSize(1))
                .body("mensagens[0].tipoAutor.nome", equalTo("CLIENTE"))
                .extract().jsonPath().getString("protocolo");
        String p2 = abrir(c.token(), null).statusCode(200).extract().jsonPath().getString("protocolo");
        org.junit.jupiter.api.Assertions.assertNotEquals(p1, p2);
    }

    @Test
    void atendenteRespondeEHistoricoFicaPreservado() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long id = abrir(c.token(), null).statusCode(200).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON).body(Map.of("texto", "Já carregou a bateria?"))
                .when().post("/admin/chamados/{id}/mensagens", id).then().statusCode(200);
        como(c.token()).contentType(ContentType.JSON).body(Map.of("texto", "Sim, carreguei."))
                .when().post("/chamados/{id}/mensagens", id).then().statusCode(200);
        como(c.token()).when().get("/chamados/{id}/mensagens", id)
                .then().statusCode(200)
                .body("tipoAutor.nome", contains("CLIENTE", "ATENDENTE", "CLIENTE"));
        como(c.token()).when().get("/conta/chamados").then().statusCode(200)
                .body("find { it.id == " + id + " }.status.nome", equalTo("EM_ANDAMENTO"));
    }

    @Test
    void chamadoEncerradoNaoRecebeMensagem() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long id = abrir(c.token(), null).statusCode(200).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON).body(Map.of("idStatus", ENCERRADO))
                .when().patch("/admin/chamados/{id}/status", id).then().statusCode(204);
        como(c.token()).contentType(ContentType.JSON).body(Map.of("texto", "Ainda tenho dúvida"))
                .when().post("/chamados/{id}/mensagens", id).then().statusCode(409);
    }

    @Test
    void pedidoDeOutroClienteResponde400() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPendente(dono, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1, Fixtures.PIX);
        abrir(outro.token(), pedido).statusCode(400).body("errors.field", hasItem("idPedido"));
    }

    @Test
    void clienteNaoVeChamadoDeOutro() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        long id = abrir(dono.token(), null).statusCode(200).extract().jsonPath().getLong("id");
        como(outro.token()).when().get("/conta/chamados").then().statusCode(200)
                .body("id", not(hasItem((int) id)));
        como(outro.token()).when().get("/chamados/{id}/mensagens", id).then().statusCode(404);
        anonimo().contentType(ContentType.JSON).body(Map.of("assunto", "x", "mensagem", "y"))
                .when().post("/chamados").then().statusCode(401);
    }
}

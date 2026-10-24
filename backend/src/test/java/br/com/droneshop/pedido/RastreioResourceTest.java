package br.com.droneshop.pedido;

import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #31 Rastreio da entrega com eventos (RF28).
 * Contrato: docs/issues/031.md
 */
@QuarkusTest
@Tag("issue-31")
class RastreioResourceTest {

    static void evento(long pedido, String descricao, String dataHora, int status) {
        given().contentType(ContentType.JSON)
                .body(Map.of("descricao", descricao, "localizacao", "Palmas/TO", "dataHora", dataHora))
                .when().post("/pedidos/{id}/rastreio", pedido).then().statusCode(status);
    }

    static long pedidoEnviado(Fixtures.Cliente c) {
        long pedido = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(100.0, 5).id(), 1);
        Fixtures.mudarStatus(pedido, Fixtures.PEDIDO_SEPARACAO).then().statusCode(204);
        Fixtures.mudarStatus(pedido, Fixtures.PEDIDO_ENVIADO).then().statusCode(204);
        return pedido;
    }

    @Test
    void clienteVeOsEventosEmOrdemCronologica() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = pedidoEnviado(c);
        given().contentType(ContentType.JSON).body(Map.of("codigoRastreio", "BR123456789BR"))
                .when().put("/pedidos/{id}/entrega/codigo-rastreio", pedido).then().statusCode(204);
        evento(pedido, "Saiu para entrega", "2026-11-02T08:30:00", 200);
        evento(pedido, "Postado", "2026-11-01T10:00:00", 200);
        como(c.token()).when().get("/conta/pedidos/{id}/rastreio", pedido)
                .then().statusCode(200)
                .body("codigoRastreio", equalTo("BR123456789BR"))
                .body("eventos.descricao", contains("Postado", "Saiu para entrega"));
    }

    @Test
    void pedidoAindaNaoEnviadoResponde409() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(100.0, 5).id(), 1);
        evento(pedido, "Cedo demais", "2026-11-01T10:00:00", 409);
    }

    @Test
    void outroClienteNaoVeORastreio() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        long pedido = pedidoEnviado(dono);
        como(outro.token()).when().get("/conta/pedidos/{id}/rastreio", pedido).then().statusCode(404);
    }
}

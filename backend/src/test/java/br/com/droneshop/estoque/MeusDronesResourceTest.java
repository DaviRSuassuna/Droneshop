package br.com.droneshop.estoque;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Issue nova "Drones registrados na conta do cliente, com série, garantia e registro ANAC".
 * Contrato: docs/issues/nova-meus-drones.md. A etiqueta é o nome do arquivo.
 * Passa pelo checkout (#26, #27) e pela expedição com número de série (#34).
 */
@QuarkusTest
@Tag("issue-meus-drones")
class MeusDronesResourceTest {

    /** Drone comprado pelo cliente e expedido com uma unidade. Devolve [pedido, unidade, produto]. */
    static long[] droneComprado(Fixtures.Cliente c, String serie) {
        long produto = Fixtures.criarDrone(3000.0, 5).id();
        long pedido = Fixtures.pedidoPago(c, produto, 1);
        Fixtures.mudarStatus(pedido, Fixtures.PEDIDO_SEPARACAO).then().statusCode(204);
        long item = given().when().get("/pedidos/{id}", pedido).then().statusCode(200)
                .extract().jsonPath().getLong("itens[0].id");
        long unidade = given().contentType(ContentType.JSON)
                .body(Map.of("idProduto", produto, "numeroSerie", serie, "registroAnac", ""))
                .when().post("/unidades").then().statusCode(200).extract().jsonPath().getLong("id");
        given().contentType(ContentType.JSON).body(Map.of("idsUnidades", List.of(unidade)))
                .when().post("/pedidos/{id}/itens/{idItem}/unidades", pedido, item).then().statusCode(204);
        return new long[] { pedido, unidade, produto };
    }

    @Test
    void clienteVeODroneCompradoComSerieEGarantia() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        String serie = "SN-" + Dados.sufixo().toUpperCase();
        long[] d = droneComprado(c, serie);
        como(c.token()).when().get("/conta/drones")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].idUnidade", equalTo((int) d[1]))
                .body("[0].produto.id", equalTo((int) d[2]))
                .body("[0].numeroSerie", equalTo(serie))
                .body("[0].pedido.id", equalTo((int) d[0]))
                .body("[0].dataCompra", notNullValue())
                .body("[0].validadeGarantia", notNullValue());
    }

    @Test
    void clienteSemCompraRecebeListaVazia() {
        como(Fixtures.novoCliente().token()).when().get("/conta/drones")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void clienteInformaORegistroAnac() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long[] d = droneComprado(c, "SN-" + Dados.sufixo().toUpperCase());
        como(c.token()).contentType(ContentType.JSON).body(Map.of("registroAnac", "PP-1234567"))
                .when().patch("/conta/drones/{id}", d[1]).then().statusCode(204);
        como(c.token()).when().get("/conta/drones")
                .then().statusCode(200)
                .body("[0].registroAnac", equalTo("PP-1234567"));
    }

    @Test
    void registroAnacLongoDemaisDa400() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long[] d = droneComprado(c, "SN-" + Dados.sufixo().toUpperCase());
        como(c.token()).contentType(ContentType.JSON).body(Map.of("registroAnac", "X".repeat(41)))
                .when().patch("/conta/drones/{id}", d[1])
                .then().statusCode(400)
                .body("errors.field", hasItem("registroAnac"));
    }

    @Test
    void outroClienteNaoVeNemAlteraADoOutro() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        long[] d = droneComprado(dono, "SN-" + Dados.sufixo().toUpperCase());
        Fixtures.Cliente outro = Fixtures.novoCliente();
        como(outro.token()).when().get("/conta/drones")
                .then().statusCode(200)
                .body("idUnidade", not(hasItem((int) d[1])));
        como(outro.token()).contentType(ContentType.JSON).body(Map.of("registroAnac", "PP-9"))
                .when().patch("/conta/drones/{id}", d[1]).then().statusCode(404);
    }

    @Test
    void semTokenDa401() {
        anonimo().when().get("/conta/drones").then().statusCode(401);
    }
}

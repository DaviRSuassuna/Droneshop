package br.com.droneshop.estoque;

import static br.com.droneshop.testes.Api.carrinho;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;

/**
 * #23 Reserva de estoque no checkout com expiração (RF12, RF13, RN01, RN02, D33).
 * Contrato: docs/issues/023.md
 */
@QuarkusTest
@Tag("issue-23")
class ReservaResourceTest {

    static int disponivel(long idProduto) {
        return given().when().get("/produtos/{id}", idProduto).then().statusCode(200)
                .extract().jsonPath().getInt("disponivel");
    }

    static void expirarTudo() {
        given().queryParam("referencia", "2099-01-01T00:00:00")
                .when().post("/admin/rotinas/expirar-reservas")
                .then().statusCode(200).body("processados", greaterThanOrEqualTo(0));
    }

    @Test
    void listaOsStatusDeReserva() {
        given().when().get("/status-reserva").then().statusCode(200)
                .body("nome", contains("ATIVA", "CONFIRMADA", "EXPIRADA"));
    }

    @Test
    void reservaDescontaDoDisponivel() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 5);
        String c = Fixtures.novoCarrinho();
        Fixtures.adicionar(c, p.id(), 2);
        carrinho(c).when().post("/carrinho/reservar")
                .then().statusCode(200)
                .body("itens[0].reservas[0].status.nome", equalTo("ATIVA"))
                .body("itens[0].reservas[0].expiraEm", notNullValue());
        org.junit.jupiter.api.Assertions.assertEquals(3, disponivel(p.id()));
    }

    @Test
    void reservarDeNovoNaoDuplica() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 5);
        String c = Fixtures.novoCarrinho();
        Fixtures.adicionar(c, p.id(), 1);
        carrinho(c).when().post("/carrinho/reservar").then().statusCode(200);
        carrinho(c).when().post("/carrinho/reservar").then().statusCode(200);
        given().queryParam("idProduto", p.id()).when().get("/estoque/reservas")
                .then().statusCode(200).body("totalItems", equalTo(1));
    }

    @Test
    void naoReservaAcimaDoDisponivel() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 2);
        String primeiro = Fixtures.novoCarrinho();
        String segundo = Fixtures.novoCarrinho();
        Fixtures.adicionar(primeiro, p.id(), 2);
        Fixtures.adicionar(segundo, p.id(), 2);
        carrinho(primeiro).when().post("/carrinho/reservar").then().statusCode(200);
        carrinho(segundo).when().post("/carrinho/reservar").then().statusCode(409);
        org.junit.jupiter.api.Assertions.assertEquals(0, disponivel(p.id()));
    }

    @Test
    void rotinaExpiraEDevolveOSaldo() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 5);
        String c = Fixtures.novoCarrinho();
        Fixtures.adicionar(c, p.id(), 3);
        carrinho(c).when().post("/carrinho/reservar").then().statusCode(200);
        expirarTudo();
        org.junit.jupiter.api.Assertions.assertEquals(5, disponivel(p.id()));
        given().queryParam("idProduto", p.id()).when().get("/estoque/reservas")
                .then().statusCode(200).body("totalItems", equalTo(0));
    }

    @Test
    void kitReservaOsComponentes() {
        long drone = Fixtures.criarDrone(1000.0, 5).id();
        long bateria = Fixtures.criarAcessorio(100.0, 6).id();
        long idKit = Fixtures.criarKit(Fixtures.componente(drone, 1), Fixtures.componente(bateria, 2)).id();

        String c = Fixtures.novoCarrinho();
        Fixtures.adicionar(c, idKit, 2);
        carrinho(c).when().post("/carrinho/reservar").then().statusCode(200)
                .body("itens[0].reservas", hasSize(2));
        org.junit.jupiter.api.Assertions.assertEquals(3, disponivel(drone));
        org.junit.jupiter.api.Assertions.assertEquals(2, disponivel(bateria));
        given().queryParam("idProduto", idKit).when().get("/estoque/reservas")
                .then().statusCode(200).body("totalItems", equalTo(0));
    }

    @Test
    void rotinaSoParaAdmin() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        como(cli.token()).when().post("/admin/rotinas/expirar-reservas").then().statusCode(403);
    }
}

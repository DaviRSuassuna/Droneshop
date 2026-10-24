package br.com.droneshop.compra;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #22 Lista de desejos (RF19).
 * Contrato: docs/issues/022.md
 */
@QuarkusTest
@Tag("issue-22")
class ListaDesejosResourceTest {

    static void favoritar(String token, long idProduto, int status) {
        como(token).contentType(ContentType.JSON).body(Map.of("idProduto", idProduto))
                .when().post("/conta/desejos").then().statusCode(status);
    }

    @Test
    void adicionaSemDuplicarERemove() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 1);
        favoritar(c.token(), p.id(), 204);
        favoritar(c.token(), p.id(), 204);
        como(c.token()).when().get("/conta/desejos")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].produto.id", equalTo((int) p.id()))
                .body("[0].produto.slug", equalTo(p.slug()));
        como(c.token()).when().delete("/conta/desejos/{id}", p.id()).then().statusCode(204);
        como(c.token()).when().get("/conta/desejos").then().statusCode(200).body("$", hasSize(0));
        como(c.token()).when().delete("/conta/desejos/{id}", p.id()).then().statusCode(404);
    }

    @Test
    void listaEDoClienteLogado() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        favoritar(dono.token(), Fixtures.criarDrone(1000.0, 1).id(), 204);
        como(outro.token()).when().get("/conta/desejos").then().statusCode(200).body("$", hasSize(0));
        anonimo().when().get("/conta/desejos").then().statusCode(401);
    }

    @Test
    void produtoInexistenteResponde400() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).contentType(ContentType.JSON).body(Map.of("idProduto", Dados.ID_INEXISTENTE))
                .when().post("/conta/desejos")
                .then().statusCode(400).body("errors.field", hasItem("idProduto"));
    }
}

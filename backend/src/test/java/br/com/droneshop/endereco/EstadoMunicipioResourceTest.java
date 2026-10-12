package br.com.droneshop.endereco;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Estados e municípios: dados oficiais do IBGE (localidades.sql), somente leitura,
 * usados nos selects do cadastro de endereço.
 */
@QuarkusTest
@Tag("base")
class EstadoMunicipioResourceTest {

    @Test
    void listaOs27EstadosEmOrdemAlfabetica() {
        given().when().get("/estados")
                .then().statusCode(200)
                .body("$", hasSize(27))
                .body("[0].nome", equalTo("Acre"))
                .body("find { it.sigla == 'TO' }.codigoIbge", equalTo(17));
    }

    @Test
    void buscaEstadoPorId() {
        long id = Dados.idEstado("TO");
        given().when().get("/estados/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Tocantins"))
                .body("sigla", equalTo("TO"));
    }

    @Test
    void listaMunicipiosDoEstadoComOEstadoAninhado() {
        given().queryParam("idEstado", Dados.idEstado("TO"))
                .when().get("/municipios")
                .then().statusCode(200)
                .body("nome", hasItem("Palmas"))
                .body("estado.sigla", everyItem(equalTo("TO")));
    }

    @Test
    void buscaMunicipioPorId() {
        long id = Dados.idMunicipio("TO", Dados.IBGE_PALMAS);
        given().when().get("/municipios/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Palmas"))
                .body("estado.nome", equalTo("Tocantins"));
    }

    @Test
    void estadoOuMunicipioInexistenteResponde404() {
        given().when().get("/estados/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().when().get("/municipios/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }

    @Test
    void listarMunicipiosSemEstadoResponde400() {
        given().when().get("/municipios")
                .then().statusCode(400)
                .body("errors.field", hasItem("idEstado"));
    }

    @Test
    void estadoInexistenteResponde400() {
        given().queryParam("idEstado", Dados.ID_INEXISTENTE)
                .when().get("/municipios")
                .then().statusCode(400)
                .body("errors.field", hasItem("idEstado"));
    }
}

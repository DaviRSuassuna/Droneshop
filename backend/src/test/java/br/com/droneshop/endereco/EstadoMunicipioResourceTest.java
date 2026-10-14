package br.com.droneshop.endereco;

import static br.com.droneshop.testes.Api.given;
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
    void listaOsEstadosPaginadosEmOrdemAlfabetica() {
        given().when().get("/estados")
                .then().statusCode(200)
                .body("items", hasSize(10))
                .body("totalItems", equalTo(27))
                .body("totalPages", equalTo(3))
                .body("items[0].nome", equalTo("Acre"));

        given().queryParam("pageSize", 27)
                .when().get("/estados")
                .then().statusCode(200)
                .body("items", hasSize(27))
                .body("items.find { it.sigla == 'TO' }.codigoIbge", equalTo(17));
    }

    @Test
    void buscaEstadoPorIdSiglaENome() {
        long id = Dados.idEstado("TO");
        given().when().get("/estados/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Tocantins"))
                .body("sigla", equalTo("TO"));
        given().when().get("/estados/sigla/{sigla}", "to")
                .then().statusCode(200)
                .body("id", equalTo((int) id));
        given().when().get("/estados/nome/{nome}", "tocant")
                .then().statusCode(200)
                .body("totalItems", equalTo(1));
    }

    @Test
    void listaMunicipiosDoEstadoComOEstadoAninhado() {
        given().queryParam("idEstado", Dados.idEstado("TO")).queryParam("pageSize", 1000)
                .when().get("/municipios")
                .then().statusCode(200)
                .body("items.nome", hasItem("Palmas"))
                .body("items.estado.sigla", everyItem(equalTo("TO")));
    }

    @Test
    void buscaMunicipioPorIdENome() {
        long id = Dados.idMunicipio("TO", Dados.IBGE_PALMAS);
        given().when().get("/municipios/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Palmas"))
                .body("estado.nome", equalTo("Tocantins"));
        given().queryParam("pageSize", 50)
                .when().get("/municipios/nome/{nome}", "palmas")
                .then().statusCode(200)
                .body("items.codigoIbge", hasItem(Dados.IBGE_PALMAS));
    }

    @Test
    void listarMunicipiosSemEstadoResponde400() {
        given().when().get("/municipios")
                .then().statusCode(400)
                .body("errors.field", hasItem("idEstado"));
    }

    @Test
    void estadoInexistenteNaListagemResponde400() {
        given().queryParam("idEstado", Dados.ID_INEXISTENTE)
                .when().get("/municipios")
                .then().statusCode(400)
                .body("errors.field", hasItem("idEstado"));
    }

    @Test
    void estadoOuMunicipioInexistenteResponde404() {
        given().when().get("/estados/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().when().get("/estados/sigla/{sigla}", "XX")
                .then().statusCode(404);
        given().when().get("/municipios/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }
}

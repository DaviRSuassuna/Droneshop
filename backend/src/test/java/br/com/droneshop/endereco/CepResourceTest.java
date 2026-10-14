package br.com.droneshop.endereco;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;

/**
 * GET /ceps/{cep}: a API consulta o ViaCEP (aqui, o ViaCepFalso) e resolve o
 * Município no banco. 200 = encontrado | 400 = formato inválido |
 * 404 = CEP não existe | 503 = ViaCEP indisponível.
 */
@QuarkusTest
@Tag("base")
class CepResourceTest {

    @Test
    void encontraOCepEResolveOMunicipioPeloCodigoIbge() {
        given().when().get("/ceps/{cep}", ViaCepFalso.CEP_PALMAS)
                .then().statusCode(200)
                .body("cep", equalTo("77001002"))
                .body("logradouro", equalTo("Quadra 104 Norte Avenida LO 2"))
                .body("complemento", nullValue())
                .body("localidade", equalTo("Palmas"))
                .body("uf", equalTo("TO"))
                .body("municipio.codigoIbge", equalTo(Dados.IBGE_PALMAS))
                .body("municipio.estado.sigla", equalTo("TO"));
    }

    @Test
    void aceitaOCepComHifen() {
        given().when().get("/ceps/{cep}", "77001-002")
                .then().statusCode(200)
                .body("cep", equalTo("77001002"));
    }

    @Test
    void semCodigoIbgeAchaOMunicipioPorNomeEUfECamposVaziosViramNulo() {
        given().when().get("/ceps/{cep}", ViaCepFalso.CEP_SEM_IBGE)
                .then().statusCode(200)
                .body("logradouro", nullValue())
                .body("bairro", nullValue())
                .body("municipio.codigoIbge", equalTo(Dados.IBGE_PALMAS));
    }

    @Test
    void cidadeSemCorrespondenciaDevolveMunicipioNulo() {
        given().when().get("/ceps/{cep}", ViaCepFalso.CEP_CIDADE_DESCONHECIDA)
                .then().statusCode(200)
                .body("localidade", equalTo("Cidade Que Nao Existe"))
                .body("municipio", nullValue());
    }

    @Test
    void formatoInvalidoResponde400SemConsultarOViaCep() {
        given().when().get("/ceps/{cep}", "1234")
                .then().statusCode(400)
                .body("errors.field", hasItem("cep"));
    }

    @Test
    void cepInexistenteResponde404() {
        given().when().get("/ceps/{cep}", ViaCepFalso.CEP_INEXISTENTE)
                .then().statusCode(404)
                .contentType("application/problem+json")
                .body("type", endsWith("/errors/not-found"));
    }

    @Test
    void viaCepForaDoArResponde503() {
        given().when().get("/ceps/{cep}", ViaCepFalso.CEP_VIACEP_FORA)
                .then().statusCode(503)
                .contentType("application/problem+json")
                .body("type", endsWith("/errors/external-service-unavailable"));
    }
}

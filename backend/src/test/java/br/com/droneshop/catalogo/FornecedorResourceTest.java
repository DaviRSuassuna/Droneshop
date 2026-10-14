package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Fornecedor (entrega anterior às issues): CRUD em /fornecedores, com endereço
 * estruturado (Município/Estado do IBGE). CNPJ, CEP e telefone vão para o banco
 * só com dígitos; a máscara é coisa do front.
 */
@QuarkusTest
@Tag("base")
class FornecedorResourceTest {

    private static Long idPalmas;

    // consultado na primeira vez que um teste precisa (a API já está de pé)
    static long idPalmas() {
        if (idPalmas == null)
            idPalmas = Dados.idMunicipio("TO", Dados.IBGE_PALMAS);
        return idPalmas;
    }

    static Map<String, Object> endereco() {
        Map<String, Object> e = new HashMap<>();
        e.put("cep", "77001-002");
        e.put("logradouro", "Quadra 104 Norte");
        e.put("numero", "12");
        e.put("complemento", "Sala 3");
        e.put("bairro", "Plano Diretor Norte");
        e.put("idMunicipio", idPalmas());
        return e;
    }

    static Map<String, Object> fornecedor(String s, String cnpj) {
        Map<String, Object> f = new HashMap<>();
        f.put("razaoSocial", "Fornecedor " + s + " Ltda.");
        f.put("cnpj", cnpj);
        f.put("nomeContato", "Contato " + s);
        f.put("emailContato", s + "@exemplo.com.br");
        f.put("telefoneContato", "(63) 99999-0000");
        f.put("endereco", endereco());
        f.put("idStatus", Dados.FORNECEDOR_ATIVO);
        return f;
    }

    static long criar(Map<String, Object> dto) {
        return given().contentType(ContentType.JSON).body(dto)
                .when().post("/fornecedores")
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    private static void esperar400(Map<String, Object> dto, String campo) {
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/fornecedores")
                .then().statusCode(400)
                .body("errors.field", hasItem(campo));
    }

    @Test
    void criaComMascarasEGuardaSoDigitos() {
        String s = Dados.sufixo();
        String cnpj = Dados.cnpjValido();
        given().contentType(ContentType.JSON).body(fornecedor(s, Dados.mascararCnpj(cnpj)))
                .when().post("/fornecedores")
                .then().statusCode(200)
                .body("cnpj", equalTo(cnpj))
                .body("telefoneContato", equalTo("63999990000"))
                .body("endereco.cep", equalTo("77001002"))
                .body("endereco.municipio.codigoIbge", equalTo(Dados.IBGE_PALMAS))
                .body("endereco.municipio.estado.sigla", equalTo("TO"))
                .body("status.nome", equalTo("Ativo"));
    }

    @Test
    void telefoneVazioViraNulo() {
        Map<String, Object> dto = fornecedor(Dados.sufixo(), Dados.cnpjValido());
        dto.put("telefoneContato", "");
        given().contentType(ContentType.JSON).body(dto)
                .when().post("/fornecedores")
                .then().statusCode(200)
                .body("telefoneContato", nullValue());
    }

    @Test
    void buscaPorCnpjSoComDigitosEPorNome() {
        String s = Dados.sufixo();
        String cnpj = Dados.cnpjValido();
        long id = criar(fornecedor(s, Dados.mascararCnpj(cnpj)));

        given().when().get("/fornecedores/cnpj/{cnpj}", cnpj)
                .then().statusCode(200)
                .body("id", equalTo((int) id));
        given().when().get("/fornecedores/nome/{nome}", s)
                .then().statusCode(200)
                .body("totalItems", equalTo(1));
    }

    @Test
    void cnpjRepetidoComOuSemMascaraResponde400() {
        String cnpj = Dados.cnpjValido();
        criar(fornecedor(Dados.sufixo(), cnpj));
        esperar400(fornecedor(Dados.sufixo(), Dados.mascararCnpj(cnpj)), "cnpj");
    }

    @Test
    void cnpjComDigitoVerificadorErradoResponde400() {
        String cnpj = Dados.cnpjValido();
        String errado = cnpj.substring(0, 13) + ((cnpj.charAt(13) - '0' + 1) % 10);
        esperar400(fornecedor(Dados.sufixo(), errado), "cnpj");
    }

    @Test
    void emailInvalidoResponde400() {
        Map<String, Object> dto = fornecedor(Dados.sufixo(), Dados.cnpjValido());
        dto.put("emailContato", "sem-arroba");
        esperar400(dto, "emailContato");
    }

    @Test
    void telefoneInvalidoResponde400() {
        Map<String, Object> dto = fornecedor(Dados.sufixo(), Dados.cnpjValido());
        dto.put("telefoneContato", "123");
        esperar400(dto, "telefoneContato");
    }

    @Test
    void semEnderecoResponde400() {
        Map<String, Object> dto = fornecedor(Dados.sufixo(), Dados.cnpjValido());
        dto.remove("endereco");
        esperar400(dto, "endereco");
    }

    @Test
    void cepInvalidoResponde400() {
        Map<String, Object> dto = fornecedor(Dados.sufixo(), Dados.cnpjValido());
        Map<String, Object> e = endereco();
        e.put("cep", "7700");
        dto.put("endereco", e);
        esperar400(dto, "cep");
    }

    @Test
    void municipioInexistenteResponde400() {
        Map<String, Object> dto = fornecedor(Dados.sufixo(), Dados.cnpjValido());
        Map<String, Object> e = endereco();
        e.put("idMunicipio", Dados.ID_INEXISTENTE);
        dto.put("endereco", e);
        esperar400(dto, "idMunicipio");
    }

    @Test
    void statusInexistenteParaFornecedorResponde400() {
        Map<String, Object> dto = fornecedor(Dados.sufixo(), Dados.cnpjValido());
        dto.put("idStatus", 3);
        esperar400(dto, "idStatus");
    }

    @Test
    void alteraEApagaFornecedor() {
        String s = Dados.sufixo();
        String cnpj = Dados.cnpjValido();
        long id = criar(fornecedor(s, cnpj));

        Map<String, Object> dto = fornecedor(s, cnpj);
        dto.put("razaoSocial", "Renomeado " + s);
        dto.put("idStatus", Dados.FORNECEDOR_INATIVO);
        given().contentType(ContentType.JSON).body(dto)
                .when().put("/fornecedores/{id}", id)
                .then().statusCode(204);
        given().when().get("/fornecedores/{id}", id)
                .then().statusCode(200)
                .body("razaoSocial", equalTo("Renomeado " + s))
                .body("status.nome", equalTo("Inativo"));

        given().when().delete("/fornecedores/{id}", id).then().statusCode(204);
        given().when().get("/fornecedores/nome/{nome}", s)
                .then().statusCode(200)
                .body("totalItems", equalTo(0));
    }

    @Test
    void inexistenteResponde404() {
        given().contentType(ContentType.JSON).body(fornecedor(Dados.sufixo(), Dados.cnpjValido()))
                .when().put("/fornecedores/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().when().get("/fornecedores/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
        given().when().get("/fornecedores/cnpj/{cnpj}", Dados.cnpjValido())
                .then().statusCode(404);
        given().when().delete("/fornecedores/{id}", Dados.ID_INEXISTENTE)
                .then().statusCode(404);
    }

    @Test
    void listaOsStatusDeFornecedor() {
        given().when().get("/status-fornecedor")
                .then().statusCode(200)
                .body("nome", hasItems("Ativo", "Inativo"));
    }
}

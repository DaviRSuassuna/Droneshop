package br.com.droneshop.identidade;

import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

/**
 * #19 Endereços do cliente, com o módulo endereco (D32) e RN15.
 * Contrato: docs/issues/019.md
 */
@QuarkusTest
@Tag("issue-19")
class EnderecoClienteResourceTest {

    static Map<String, Object> endereco(String apelido, boolean principal) {
        Map<String, Object> e = new HashMap<>();
        e.put("apelido", apelido);
        e.put("cep", "77001-002");
        e.put("logradouro", "Quadra 104 Norte");
        e.put("numero", "10");
        e.put("complemento", "");
        e.put("bairro", "Plano Diretor Norte");
        e.put("idMunicipio", Fixtures.idPalmas());
        e.put("principal", principal);
        return e;
    }

    static long criar(String token, Map<String, Object> e) {
        return como(token).contentType(ContentType.JSON).body(e).when().post("/conta/enderecos")
                .then().statusCode(200).extract().jsonPath().getLong("id");
    }

    @Test
    void primeiroEnderecoESemprePrincipal() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).contentType(ContentType.JSON).body(endereco("Casa", false))
                .when().post("/conta/enderecos")
                .then().statusCode(200)
                .body("principal", equalTo(true))
                .body("cep", equalTo("77001002"))
                .body("municipio.nome", equalTo("Palmas"))
                .body("municipio.estado.sigla", equalTo("TO"));
    }

    @Test
    void soUmPrincipalPorCliente() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        criar(c.token(), endereco("Casa", true));
        long trabalho = criar(c.token(), endereco("Trabalho", false));
        como(c.token()).when().patch("/conta/enderecos/{id}/principal", trabalho).then().statusCode(204);
        como(c.token()).when().get("/conta/enderecos")
                .then().statusCode(200)
                .body("findAll { it.principal }", hasSize(1))
                .body("find { it.principal }.apelido", equalTo("Trabalho"));
    }

    @Test
    void excluirOPrincipalPassaAVezAoMaisAntigo() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        criar(c.token(), endereco("Casa", true));
        long trabalho = criar(c.token(), endereco("Trabalho", true));
        como(c.token()).when().delete("/conta/enderecos/{id}", trabalho).then().statusCode(204);
        como(c.token()).when().get("/conta/enderecos")
                .then().statusCode(200).body("find { it.principal }.apelido", equalTo("Casa"));
    }

    @Test
    void camposObrigatorios() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        Map<String, Object> e = endereco("Casa", true);
        e.put("cep", "123");
        e.remove("idMunicipio");
        e.put("numero", "");
        como(c.token()).contentType(ContentType.JSON).body(e).when().post("/conta/enderecos")
                .then().statusCode(400)
                .body("errors.field", hasItem("cep"))
                .body("errors.field", hasItem("idMunicipio"))
                .body("errors.field", hasItem("numero"));
    }

    @Test
    void enderecoDeOutroClienteResponde404() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        long id = criar(dono.token(), endereco("Casa", true));
        como(outro.token()).contentType(ContentType.JSON).body(endereco("Invasao", true))
                .when().put("/conta/enderecos/{id}", id).then().statusCode(404);
        como(outro.token()).when().delete("/conta/enderecos/{id}", id).then().statusCode(404);
    }

    /** RN15: o cliente semente da carga (#16) tem pedidos; editar o endereço não muda nenhum. */
    @Test
    void editarEnderecoNaoMudaOPedido() {
        String token = Api.login(Api.CLIENTE_EMAIL, Api.CLIENTE_SENHA);
        String nome = como(token).when().get("/auth/eu").then().statusCode(200).extract().jsonPath().getString("nome");
        long idPedido = given().queryParam("busca", nome).queryParam("pageSize", 1).when().get("/pedidos")
                .then().statusCode(200).extract().jsonPath().getLong("items[0].id");
        JsonPath antes = given().when().get("/pedidos/{id}", idPedido).then().statusCode(200).extract().jsonPath();

        long idEndereco = como(token).when().get("/conta/enderecos").then().statusCode(200)
                .extract().jsonPath().getLong("[0].id");
        Map<String, Object> e = endereco("Mudou", true);
        e.put("logradouro", "Rua Nova Depois do Pedido");
        como(token).contentType(ContentType.JSON).body(e).when().put("/conta/enderecos/{id}", idEndereco)
                .then().statusCode(204);

        given().when().get("/pedidos/{id}", idPedido)
                .then().statusCode(200)
                .body("enderecoEntrega.logradouro", equalTo(antes.getString("enderecoEntrega.logradouro")));
    }
}

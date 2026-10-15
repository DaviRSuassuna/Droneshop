package br.com.droneshop.identidade;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #19 Criação de conta e PerfilCliente (RF48, D1).
 * Contrato: docs/issues/019.md
 */
@QuarkusTest
@Tag("issue-19")
class ClienteResourceTest {

    static ValidatableResponse criarConta(Map<String, Object> conta) {
        return anonimo().contentType(ContentType.JSON).body(conta).when().post("/clientes").then();
    }

    static String email() {
        return Dados.sufixo() + "@cliente.com.br";
    }

    @Test
    void listaTiposDePessoa() {
        anonimo().when().get("/tipos-pessoa").then().statusCode(200).body("nome", contains("PF", "PJ"));
    }

    @Test
    void criaContaPfEEntraComoCliente() {
        String email = email();
        criarConta(Fixtures.novaConta(email)).statusCode(200)
                .body("id", notNullValue())
                .body("email", equalTo(email))
                .body("tipoPessoa.nome", equalTo("PF"))
                .body(not(containsString("senha")));
        Api.tentarLogin(email, "segredo123").then().statusCode(200).body("perfis", contains("CLIENTE"));
    }

    @Test
    void criaContaPjComCnpj() {
        Map<String, Object> c = Fixtures.novaConta(email());
        c.put("idTipoPessoa", Fixtures.PJ);
        c.put("documento", Dados.cnpjValido());
        criarConta(c).statusCode(200).body("tipoPessoa.nome", equalTo("PJ"));
    }

    @Test
    void documentoInvalidoParaOTipoResponde400() {
        Map<String, Object> pf = Fixtures.novaConta(email());
        pf.put("documento", "12345678900");
        criarConta(pf).statusCode(400).body("errors.field", hasItem("documento"));

        Map<String, Object> pj = Fixtures.novaConta(email());
        pj.put("idTipoPessoa", Fixtures.PJ); // CPF num cadastro PJ
        criarConta(pj).statusCode(400).body("errors.field", hasItem("documento"));
    }

    @Test
    void documentoEEmailRepetidosRespondem400() {
        Map<String, Object> c = Fixtures.novaConta(email());
        criarConta(c).statusCode(200);
        Map<String, Object> mesmoDocumento = Fixtures.novaConta(email());
        mesmoDocumento.put("documento", c.get("documento"));
        criarConta(mesmoDocumento).statusCode(400).body("errors.field", hasItem("documento"));
        Map<String, Object> mesmoEmail = Fixtures.novaConta((String) c.get("email"));
        criarConta(mesmoEmail).statusCode(400).body("errors.field", hasItem("email"));
    }

    @Test
    void senhaCurtaResponde400() {
        Map<String, Object> c = Fixtures.novaConta(email());
        c.put("senha", "123");
        criarConta(c).statusCode(400).body("errors.field", hasItem("senha"));
    }

    @Test
    void documentoComMascaraEGuardadoSoComDigitos() {
        Map<String, Object> c = Fixtures.novaConta(email());
        String cpf = (String) c.get("documento");
        c.put("documento", cpf.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4"));
        c.put("telefone", "(63) 99999-0000");
        criarConta(c).statusCode(200).body("documento", equalTo(cpf)).body("telefone", equalTo("63999990000"));
    }

    @Test
    void perfilDoClienteLogado() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        como(cli.token()).when().get("/conta/perfil")
                .then().statusCode(200).body("email", equalTo(cli.email()));
        como(cli.token()).contentType(ContentType.JSON).body(Map.of("nome", "Novo Nome", "telefone", "63988887777"))
                .when().put("/conta/perfil").then().statusCode(204);
        como(cli.token()).when().get("/conta/perfil")
                .then().statusCode(200).body("nome", equalTo("Novo Nome"));
    }

    @Test
    void trocaDeSenhaConfereASenhaAtual() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        como(cli.token()).contentType(ContentType.JSON).body(Map.of("senhaAtual", "errada", "senhaNova", "outra12345"))
                .when().put("/conta/senha").then().statusCode(400).body("errors.field", hasItem("senhaAtual"));
        como(cli.token()).contentType(ContentType.JSON).body(Map.of("senhaAtual", cli.senha(), "senhaNova", "outra12345"))
                .when().put("/conta/senha").then().statusCode(204);
        Api.tentarLogin(cli.email(), "outra12345").then().statusCode(200);
    }

    @Test
    void contaExigeClienteLogado() {
        anonimo().when().get("/conta/perfil").then().statusCode(401);
        given().when().get("/conta/perfil").then().statusCode(403); // admin sem perfil de cliente
    }

    @Test
    void clienteNaoEntraNoAdmin() {
        Fixtures.Cliente cli = Fixtures.novoCliente();
        como(cli.token()).when().get("/marcas").then().statusCode(403);
    }
}

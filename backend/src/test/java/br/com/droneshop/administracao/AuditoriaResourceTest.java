package br.com.droneshop.administracao;

import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #41 Log de auditoria das ações administrativas (RF45, RNF03, RNF11).
 * Contrato: docs/issues/041.md
 */
@QuarkusTest
@Tag("issue-41")
class AuditoriaResourceTest {

    @Test
    void listaAsAcoes() {
        given().when().get("/acoes-auditoria").then().statusCode(200)
                .body("nome", contains("CRIACAO", "ATUALIZACAO", "EXCLUSAO", "LOGIN", "MUDANCA_STATUS"));
    }

    @Test
    void alteracaoDePrecoGuardaValorAnteriorENovo() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 2);
        Map<String, Object> corpo = Fixtures.corpoDoProduto(p.id());
        corpo.put("preco", 1200.0);
        Fixtures.alterarProduto(p.id(), corpo).then().statusCode(204);
        given().queryParam("entidade", "Produto").queryParam("entidadeId", p.id())
                .queryParam("pageSize", 50)
                .when().get("/admin/auditoria")
                .then().statusCode(200)
                .body("items.find { it.campo == 'preco' }.acao.nome", equalTo("ATUALIZACAO"))
                .body("items.find { it.campo == 'preco' }.valorNovo", equalTo("1200.00"))
                .body("items.find { it.campo == 'preco' }.autor.id", notNullValue())
                .body("items.find { it.campo == 'preco' }.ip", notNullValue());
    }

    @Test
    void mudancaDeStatusDePedidoEAuditada() {
        long pedido = Fixtures.pedidoDaCarga(Fixtures.PEDIDO_PAGO);
        Fixtures.mudarStatus(pedido, Fixtures.PEDIDO_SEPARACAO).then().statusCode(204);
        given().queryParam("entidade", "Pedido").queryParam("entidadeId", pedido)
                .when().get("/admin/auditoria")
                .then().statusCode(200)
                .body("items.acao.nome", hasItem("MUDANCA_STATUS"));
    }

    @Test
    void loginNoAdminEAuditado() {
        Api.login(Api.ADMIN_EMAIL, Api.ADMIN_SENHA);
        given().queryParam("idAcao", 4).when().get("/admin/auditoria")
                .then().statusCode(200).body("totalItems", org.hamcrest.Matchers.greaterThan(0));
    }

    @Test
    void dadoPessoalNaoVaiProLog() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).contentType(ContentType.JSON).body(Map.of("nome", "Outro Nome", "telefone", "63911112222"))
                .when().put("/conta/perfil").then().statusCode(204);
        given().queryParam("pageSize", 200).when().get("/admin/auditoria")
                .then().statusCode(200)
                .body("items.valorNovo", not(hasItem("63911112222")))
                .body("items.valorAnterior", not(hasItem("63999990000")));
    }

    @Test
    void soAdminConsulta() {
        String token = Fixtures.adminComPapel(Fixtures.PAPEL_OPERADOR);
        como(token).when().get("/admin/auditoria").then().statusCode(403);
    }
}

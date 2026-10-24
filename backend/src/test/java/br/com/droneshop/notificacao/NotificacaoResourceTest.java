package br.com.droneshop.notificacao;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

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
 * #32 E-mails transacionais via Resend com templates editáveis (RF38, RF39, RF40, D5, RNF05).
 * Contrato: docs/issues/032.md. O provedor é o ResendFalso.
 */
@QuarkusTest
@Tag("issue-32")
class NotificacaoResourceTest {

    static ValidatableResponse notificacoes(String email) {
        return given().queryParam("destinatario", email).queryParam("pageSize", 50)
                .when().get("/admin/notificacoes").then().statusCode(200);
    }

    @Test
    void templatesDaCarga() {
        given().when().get("/admin/templates-email").then().statusCode(200)
                .body("codigo", hasItems("PEDIDO_CONFIRMADO", "PAGAMENTO_APROVADO", "PEDIDO_ENVIADO",
                        "RASTREIO_ATUALIZADO", "DEVOLUCAO_ATUALIZADA", "CHAMADO_RESPONDIDO",
                        "VERIFICAR_EMAIL", "RECUPERAR_SENHA", "CARRINHO_ABANDONADO"));
    }

    @Test
    void editarTemplateCriaNovaVersao() {
        Map<String, Object> atual = given().when().get("/admin/templates-email/{codigo}", "PEDIDO_ENVIADO")
                .then().statusCode(200).extract().jsonPath().getMap("$");
        int versao = (Integer) atual.get("versao");
        given().contentType(ContentType.JSON)
                .body(Map.of("assunto", "Seu pedido saiu", "corpoTemplate", atual.get("corpoTemplate")))
                .when().put("/admin/templates-email/{codigo}", "PEDIDO_ENVIADO")
                .then().statusCode(200)
                .body("versao", equalTo(versao + 1))
                .body("assunto", equalTo("Seu pedido saiu"));
    }

    @Test
    void variavelQueOTemplateNaoOfereceResponde400() {
        given().contentType(ContentType.JSON)
                .body(Map.of("assunto", "x", "corpoTemplate", "Olá {{variavelQueNaoExiste}}"))
                .when().put("/admin/templates-email/{codigo}", "PEDIDO_CONFIRMADO")
                .then().statusCode(400).body("errors.field", hasItem("corpoTemplate"));
    }

    @Test
    void pedidoEPagamentoDisparamEmail() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        Fixtures.pedidoPago(c, Fixtures.criarAcessorio(100.0, 5).id(), 1);
        anonimo().queryParam("para", c.email()).when().get("/resend-falso/emails")
                .then().statusCode(200).body("size()", greaterThanOrEqualTo(2));
        notificacoes(c.email())
                .body("items.template.codigo", hasItems("PEDIDO_CONFIRMADO", "PAGAMENTO_APROVADO"))
                .body("items.categoria.nome", hasItem("TRANSACIONAL"));
    }

    @Test
    void webhookAtualizaOStatusDeEntrega() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(100.0, 5).id(), 1, Fixtures.PIX);
        String idProvedor = notificacoes(c.email()).extract().jsonPath().getString("items[0].identificadorProvedor");
        anonimo().contentType(ContentType.JSON)
                .body(Map.of("type", "email.opened", "data", Map.of("email_id", idProvedor)))
                .when().post("/webhooks/resend").then().statusCode(200);
        notificacoes(c.email()).body("items[0].statusEnvio.nome", equalTo("ABERTA"));
    }

    @Test
    void falhaNoEnvioNaoDerrubaOPedido() {
        String email = "falha-envio-" + Dados.sufixo() + "@cliente.com.br";
        Api.anonimo().contentType(ContentType.JSON).body(Fixtures.novaConta(email))
                .when().post("/clientes").then().statusCode(200);
        Fixtures.Cliente c = new Fixtures.Cliente(0, email, "segredo123", Api.login(email, "segredo123"));
        long pedido = Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(100.0, 5).id(), 1, Fixtures.PIX);
        given().when().get("/pedidos/{id}", pedido).then().statusCode(200);
        notificacoes(email).body("items[0].statusEnvio.nome", equalTo("FALHA"));
    }
}

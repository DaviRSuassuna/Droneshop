package br.com.droneshop.administracao;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Issue nova "Ações em lote, reordenação de categorias, exportação CSV e
 * etiqueta de pedido" (D36). Contrato: docs/issues/nova-lote-csv.md.
 * A etiqueta não tem número de issue: é o nome do arquivo.
 */
@QuarkusTest
@Tag("issue-lote-csv")
class AcoesAdminResourceTest {

    @Test
    void listagensTrazemAQuantidadeDeProdutos() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.criarDrone(a, 1000.0, 1);
        Fixtures.criarAcessorio(a, 100.0, 1);
        given().when().get("/marcas/{id}", a.idMarca()).then().statusCode(200)
                .body("quantidadeProdutos", equalTo(2));
        given().queryParam("pageSize", 1000).when().get("/fornecedores").then().statusCode(200)
                .body("items.find { it.id == " + a.idFornecedor() + " }.quantidadeProdutos", equalTo(2));
    }

    @Test
    void loteInativaEInformaFalhas() {
        Fixtures.ProdutoCriado p1 = Fixtures.criarDrone(1000.0, 1);
        Fixtures.ProdutoCriado p2 = Fixtures.criarDrone(1000.0, 1);
        given().contentType(ContentType.JSON)
                .body(Map.of("ids", List.of(p1.id(), p2.id(), Dados.ID_INEXISTENTE), "acao", "INATIVAR"))
                .when().patch("/produtos/lote")
                .then().statusCode(200)
                .body("processados", equalTo(2))
                .body("falhas", hasSize(1))
                .body("falhas[0].id", equalTo((int) Dados.ID_INEXISTENTE))
                .body("falhas[0].motivo", notNullValue());
        given().when().get("/produtos/{id}", p1.id()).then().body("status.id", equalTo((int) Fixtures.PRODUTO_INATIVO));
    }

    @Test
    void reajustaPrecoEmLote() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 1);
        given().contentType(ContentType.JSON)
                .body(Map.of("ids", List.of(p.id()), "acao", "REAJUSTAR_PRECO", "percentual", 10.0))
                .when().patch("/produtos/lote").then().statusCode(200).body("processados", equalTo(1));
        given().when().get("/variantes/{id}", p.idVariante()).then().body("preco", equalTo(1100.0f));
    }

    @Test
    void reordenaCategorias() {
        long c1 = Fixtures.arvore().idCategoria();
        long c2 = Fixtures.arvore().idCategoria();
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of(c2, c1)))
                .when().patch("/categorias/ordem").then().statusCode(204);
        given().when().get("/categorias/{id}", c2).then().body("ordemExibicao", equalTo(1));
        given().when().get("/categorias/{id}", c1).then().body("ordemExibicao", equalTo(2));
    }

    @Test
    void exportaCsvComBomEPontoEVirgula() {
        Fixtures.Arvore a = Fixtures.arvore();
        byte[] csv = given().when().get("/marcas/exportar")
                .then().statusCode(200).contentType(startsWith("text/csv"))
                .extract().asByteArray();
        org.junit.jupiter.api.Assertions.assertEquals((byte) 0xEF, csv[0]);
        String texto = new String(csv, java.nio.charset.StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(texto.lines().findFirst().orElse("").contains(";"));
        org.junit.jupiter.api.Assertions.assertTrue(texto.contains("Marca " + a.slugMarca().substring(6)));
    }

    @Test
    void etiquetaDoPedido() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1, Fixtures.PIX);
        given().when().get("/pedidos/{id}/etiqueta", pedido).then().statusCode(200).contentType("application/pdf");
    }
}

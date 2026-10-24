package br.com.droneshop.estoque;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #13 MovimentoEstoque com origem e autor (RF14, D11).
 * Contrato: docs/issues/013.md
 */
@QuarkusTest
@Tag("issue-13")
class MovimentoEstoqueResourceTest {

    static ValidatableResponse movimento(long idProduto, long idTipo, int quantidade) {
        Map<String, Object> m = new HashMap<>();
        m.put("idProduto", idProduto);
        m.put("idTipo", idTipo);
        m.put("quantidade", quantidade);
        m.put("observacao", "teste");
        return given().contentType(ContentType.JSON).body(m).when().post("/estoque/movimentos").then();
    }

    static int estoque(long idProduto) {
        return given().when().get("/produtos/{id}", idProduto).then().statusCode(200)
                .extract().jsonPath().getInt("estoqueFisico");
    }

    @Test
    void listaTiposEOrigens() {
        given().when().get("/tipos-movimento").then().statusCode(200)
                .body("nome", contains("ENTRADA", "SAIDA", "AJUSTE", "DEVOLUCAO", "PERDA"));
        given().when().get("/origens-registro").then().statusCode(200)
                .body("nome", contains("MANUAL", "SISTEMA", "INTEGRACAO"));
    }

    @Test
    void criarProdutoComSaldoGeraEntradaManual() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 8);
        given().queryParam("idProduto", p.id()).when().get("/estoque/movimentos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].tipo.nome", equalTo("ENTRADA"))
                .body("items[0].quantidade", equalTo(8))
                .body("items[0].saldoResultante", equalTo(8));
    }

    @Test
    void entradaESaidaAtualizamOSaldoComAutor() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 10);
        movimento(p.id(), Fixtures.MOV_ENTRADA, 5).statusCode(200)
                .body("origem.nome", equalTo("MANUAL"))
                .body("autor.id", notNullValue())
                .body("saldoResultante", equalTo(15));
        movimento(p.id(), Fixtures.MOV_SAIDA, 3).statusCode(200)
                .body("saldoResultante", equalTo(12));
        movimento(p.id(), Fixtures.MOV_PERDA, 2).statusCode(200)
                .body("saldoResultante", equalTo(10));
        org.junit.jupiter.api.Assertions.assertEquals(10, estoque(p.id()));
    }

    @Test
    void ajusteRecebeOSaldoNovo() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 10);
        movimento(p.id(), Fixtures.MOV_AJUSTE, 7).statusCode(200)
                .body("saldoResultante", equalTo(7));
        movimento(p.id(), Fixtures.MOV_AJUSTE, 0).statusCode(200)
                .body("saldoResultante", equalTo(0));
    }

    @Test
    void saidaAcimaDoSaldoResponde409ENadaMuda() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 2);
        movimento(p.id(), Fixtures.MOV_SAIDA, 3).statusCode(409);
        org.junit.jupiter.api.Assertions.assertEquals(2, estoque(p.id()));
    }

    @Test
    void quantidadeZeroResponde400() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 2);
        movimento(p.id(), Fixtures.MOV_ENTRADA, 0).statusCode(400)
                .body("errors.field", hasItem("quantidade"));
    }

    @Test
    void kitNaoRecebeMovimento() {
        long idComponente = Fixtures.criarDrone(1000.0, 5).id();
        long idKit = Fixtures.criarKit(Fixtures.componente(idComponente, 1)).id();
        movimento(idKit, Fixtures.MOV_ENTRADA, 1).statusCode(400).body("errors.field", hasItem("idProduto"));
    }

    @Test
    void produtoInexistenteResponde404() {
        movimento(Dados.ID_INEXISTENTE, Fixtures.MOV_ENTRADA, 1).statusCode(404);
    }

    @Test
    void visaoDeEstoqueMarcaAbaixoDoMinimo() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1000.0, 5);
        movimento(p.id(), Fixtures.MOV_AJUSTE, 0).statusCode(200);
        given().queryParam("situacao", "ABAIXO_MINIMO").queryParam("busca", p.sku())
                .when().get("/estoque")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].produto.id", equalTo((int) p.id()))
                .body("items[0].abaixoDoMinimo", equalTo(true))
                .body("items[0].disponivel", equalTo(0));
    }
}

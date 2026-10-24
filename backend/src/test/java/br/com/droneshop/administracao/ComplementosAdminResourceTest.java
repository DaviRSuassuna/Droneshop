package br.com.droneshop.administracao;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import br.com.droneshop.testes.Fixtures;
import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@Tag("issue-61")
class ComplementosAdminResourceTest {
    @Test
    void contaProdutosNasQuatroRelacoesSemConfundirTipos() {
        var a = Fixtures.arvore();
        Fixtures.criarDrone(a, 1000, 2);
        Fixtures.criarAcessorio(a, 100, 3);
        given().get("/marcas/{id}", a.idMarca()).then().statusCode(200).body("quantidadeProdutos", equalTo(2));
        given().get("/categorias/{id}", a.idCategoria()).then().statusCode(200).body("quantidadeProdutos", equalTo(1));
        given().get("/subcategorias/{id}", a.idSubcategoria()).then().statusCode(200).body("quantidadeProdutos", equalTo(1));
        given().get("/fornecedores/{id}", a.idFornecedor()).then().statusCode(200).body("quantidadeProdutos", equalTo(2));
    }

    @Test
    void ativacaoInvalidaNaoImpedeProdutoValidoENaoMudaEstoque() {
        long rascunho = given().contentType(ContentType.JSON).body(Map.of("idStatus", 1)).post("/drones")
                .then().statusCode(200).extract().jsonPath().getLong("id");
        var valido = Fixtures.criarDrone(1000, 5);
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of(rascunho, valido.id()), "acao", "ATIVAR"))
                .patch("/produtos/lote").then().statusCode(200).body("processados", equalTo(1)).body("falhas", hasSize(1));
        given().get("/produtos/{id}", rascunho).then().body("status.id", equalTo(1));
        given().get("/produtos/{id}", valido.id()).then().body("status.id", equalTo(2)).body("estoqueFisico", equalTo(5));
    }

    @Test
    void moverRespeitaOTipoDaCategoria() {
        var a = Fixtures.arvore();
        var drone = Fixtures.criarDrone(a, 1000, 1);
        given().contentType(ContentType.JSON)
                .body(Map.of("ids", List.of(drone.id()), "acao", "MOVER_SUBCATEGORIA", "idSubcategoria", a.idSubcategoriaAcessorio()))
                .patch("/produtos/lote").then().statusCode(200).body("processados", equalTo(0)).body("falhas", hasSize(1));
        given().get("/produtos/{id}", drone.id()).then().body("subcategoria.id", equalTo((int) a.idSubcategoria()));
    }

    @Test
    void reajusteNaoAceitaPrecoNegativoENemProcessaIdDuasVezes() {
        var p = Fixtures.criarDrone(1000, 2);
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of(p.id()), "acao", "REAJUSTAR_PRECO", "percentual", -110))
                .patch("/produtos/lote").then().statusCode(200).body("processados", equalTo(0)).body("falhas", hasSize(1));
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of(p.id(), p.id()), "acao", "REAJUSTAR_PRECO", "percentual", 10))
                .patch("/produtos/lote").then().statusCode(200).body("processados", equalTo(1));
        given().get("/produtos/{id}", p.id()).then().body("preco", equalTo(1100f)).body("estoqueFisico", equalTo(2));
    }

    @Test
    void rejeitaLoteVazioAcaoDesconhecidaECamposAusentes() {
        for (Map<String, Object> dto : List.<Map<String, Object>>of(
                Map.of("ids", List.of(), "acao", "ATIVAR"),
                Map.of("ids", List.of(1), "acao", "APAGAR"),
                Map.of("ids", List.of(1), "acao", "REAJUSTAR_PRECO"),
                Map.of("ids", List.of(1), "acao", "MOVER_SUBCATEGORIA"))) {
            given().contentType(ContentType.JSON).body(dto).patch("/produtos/lote").then().statusCode(400);
        }
    }

    @Test
    void ordemInvalidaNaoAlteraCategorias() {
        long id = Fixtures.arvore().idCategoria();
        int antes = given().get("/categorias/{id}", id).then().extract().jsonPath().getInt("ordemExibicao");
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of(id, Dados.ID_INEXISTENTE)))
                .patch("/categorias/ordem").then().statusCode(404);
        given().get("/categorias/{id}", id).then().body("ordemExibicao", equalTo(antes));
        given().contentType(ContentType.JSON).body(Map.of("ids", List.of(id, id)))
                .patch("/categorias/ordem").then().statusCode(400).body("errors.field", hasItem("ids"));
    }

    @Test
    void csvUsaFiltroNaoIncluiProdutoDeOutraMarca() {
        var a = Fixtures.arvore();
        var incluido = Fixtures.criarDrone(a, 1234, 7);
        var excluido = Fixtures.criarDrone(999, 1);
        given().queryParam("idMarca", a.idMarca()).get("/produtos/exportar").then().statusCode(200)
                .header("Content-Disposition", containsString("produtos.csv"))
                .body(containsString(incluido.sku())).body(not(containsString(excluido.sku())));
        given().queryParam("idMarca", a.idMarca()).get("/estoque/exportar").then().statusCode(200)
                .body(containsString(incluido.sku())).body(not(containsString(excluido.sku())));
    }
    @Test
    void buscaFornecedorPorEmailValeParaListagemEExportacao() {
        var a = Fixtures.arvore();
        String email = given().get("/fornecedores/{id}", a.idFornecedor()).then().statusCode(200)
                .extract().jsonPath().getString("emailContato");
        given().queryParam("busca", email).get("/fornecedores").then().statusCode(200)
                .body("items.id", hasItem((int) a.idFornecedor()));
        given().queryParam("busca", email).get("/fornecedores/exportar").then().statusCode(200)
                .body(containsString(email));
    }

    @Test
    void novasRotasExigemAutenticacao() {
        for (String rota : List.of("/marcas/exportar", "/fornecedores/exportar", "/produtos/exportar", "/estoque/exportar", "/pedidos/exportar", "/pedidos/1/etiqueta"))
            br.com.droneshop.testes.Api.anonimo().get(rota).then().statusCode(401);
    }
}

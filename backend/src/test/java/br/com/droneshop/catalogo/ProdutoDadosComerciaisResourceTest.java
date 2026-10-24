package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
 * #80 Produto (D40): SKU, preço, custo, estoque e embalagem no próprio produto
 * (substitui a VarianteProduto da #9). RN18, D26, D28.
 * Contrato: docs/issues/080.md
 */
@QuarkusTest
@Tag("issue-80")
class ProdutoDadosComerciaisResourceTest {

    static String sku() {
        return "SKU-" + Dados.sufixo().toUpperCase();
    }

    static ValidatableResponse postDrone(Map<String, Object> dadosComerciais) {
        return given().contentType(ContentType.JSON)
                .body(Fixtures.drone("Drone " + Dados.sufixo(), Fixtures.arvore(), dadosComerciais))
                .when().post("/drones").then();
    }

    @Test
    void criaComMargemCalculadaEDisponivel() {
        String sku = sku();
        Map<String, Object> v = Fixtures.dadosComerciais(sku.toLowerCase(), 1000.0, 10);
        v.put("custoAquisicao", 600.0);
        postDrone(v).statusCode(200)
                .body("sku", equalTo(sku))
                .body("preco", equalTo(1000.0f))
                .body("custoAquisicao", equalTo(600.0f))
                .body("margemPercentual", equalTo(40.0f))
                .body("estoqueFisico", equalTo(10))
                .body("disponivel", equalTo(10))
                .body("pesoEmbalagemKg", equalTo(1.5f))
                .body("grupo", nullValue())
                .body("variantes", nullValue());
    }

    @Test
    void skuRepetidoEmQualquerProdutoResponde400() {
        String sku = sku();
        postDrone(Fixtures.dadosComerciais(sku, 100.0, 1)).statusCode(200);
        given().contentType(ContentType.JSON)
                .body(Fixtures.acessorio("Acessorio " + Dados.sufixo(), Fixtures.arvore(), Fixtures.dadosComerciais(sku, 100.0, 1)))
                .when().post("/acessorios").then().statusCode(400)
                .body("errors.field", hasItem("sku"));
    }

    @Test
    void ativoSemPrecoOuComPrecoZeroResponde400() {
        Map<String, Object> semPreco = Fixtures.dadosComerciais(sku(), 100.0, 1);
        semPreco.remove("preco");
        postDrone(semPreco).statusCode(400).body("errors.field", hasItem("preco"));
        postDrone(Fixtures.dadosComerciais(sku(), 0.0, 1)).statusCode(400).body("errors.field", hasItem("preco"));
    }

    @Test
    void ativoSemEmbalagemNaoPublica() {
        Map<String, Object> v = Fixtures.dadosComerciais(sku(), 100.0, 1);
        v.remove("pesoEmbalagemKg");
        v.remove("comprimentoEmbalagemCm");
        postDrone(v).statusCode(400)
                .body("errors.field", hasItem("pesoEmbalagemKg"))
                .body("errors.field", hasItem("comprimentoEmbalagemCm"));
    }

    @Test
    void inativoTambemExigePrecoEEmbalagem() {
        Map<String, Object> v = Fixtures.dadosComerciais(sku(), 100.0, 1);
        v.remove("preco");
        v.remove("alturaEmbalagemCm");
        Map<String, Object> d = Fixtures.drone("Inativo " + Dados.sufixo(), Fixtures.arvore(), v);
        d.put("idStatus", Fixtures.PRODUTO_INATIVO);
        given().contentType(ContentType.JSON).body(d).when().post("/drones").then().statusCode(400)
                .body("errors.field", hasItem("preco"))
                .body("errors.field", hasItem("alturaEmbalagemCm"));
    }

    @Test
    void rascunhoAceitaPrecoEEmbalagemVazios() {
        Map<String, Object> d = new HashMap<>();
        d.put("nome", "Rascunho " + Dados.sufixo());
        d.put("sku", sku());
        d.put("idStatus", Fixtures.PRODUTO_RASCUNHO);
        given().contentType(ContentType.JSON).body(d).when().post("/drones").then().statusCode(200)
                .body("preco", nullValue())
                .body("estoqueFisico", equalTo(0));
    }

    @Test
    void putIgnoraOEstoqueFisico() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1500.0, 3);
        Map<String, Object> corpo = Fixtures.corpoDoProduto(p.id());
        corpo.put("preco", 1800.0);
        corpo.put("estoqueFisico", 999);
        Fixtures.alterarProduto(p.id(), corpo).then().statusCode(204);
        given().when().get("/produtos/{id}", p.id())
                .then().statusCode(200)
                .body("preco", equalTo(1800.0f))
                .body("estoqueFisico", equalTo(3));
    }

    @Test
    void buscaPorNomeOuSkuTrazOsCamposDoSelect() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(1500.0, 3);
        given().queryParam("busca", p.sku().toLowerCase()).when().get("/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) p.id()))
                .body("items[0].sku", equalTo(p.sku()))
                .body("items[0].preco", equalTo(1500.0f))
                .body("items[0].estoqueFisico", equalTo(3))
                .body("items[0].tipo.nome", equalTo("DRONE"))
                .body("items", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.hasKey("grupo")));
    }

    @Test
    void statusAtivoListaSoAtivosEKitFalseTiraOsKits() {
        String s = Dados.sufixo();
        Fixtures.Arvore a = Fixtures.arvore();
        long ativo = Fixtures.criar("/drones", Fixtures.drone("Select " + s + " A", a, Fixtures.dadosComerciais(sku(), 100.0, 2)));
        Map<String, Object> rascunho = Fixtures.drone("Select " + s + " B", a, Fixtures.dadosComerciais(sku(), 100.0, 2));
        rascunho.put("idStatus", Fixtures.PRODUTO_RASCUNHO);
        long idRascunho = Fixtures.criar("/drones", rascunho);
        long kit = Fixtures.criar("/kits", Fixtures.kit("Select " + s + " C", a,
                Fixtures.lista(Fixtures.componente(ativo, 1))));

        given().queryParam("busca", "select " + s).queryParam("idStatus", Fixtures.PRODUTO_ATIVO).queryParam("pageSize", 50)
                .when().get("/produtos")
                .then().statusCode(200)
                .body("items.id", hasItem((int) ativo))
                .body("items.id", hasItem((int) kit))
                .body("items.id", not(hasItem((int) idRascunho)));
        given().queryParam("busca", "select " + s).queryParam("idStatus", Fixtures.PRODUTO_ATIVO).queryParam("kit", false)
                .queryParam("pageSize", 50)
                .when().get("/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) ativo));
    }

    @Test
    void criarComSaldoGeraMovimentoEntrada() {
        Fixtures.ProdutoCriado p = Fixtures.criarAcessorio(200.0, 6);
        given().queryParam("idProduto", p.id()).when().get("/estoque/movimentos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].produto.sku", equalTo(p.sku()))
                .body("items[0].saldoResultante", equalTo(6))
                .body("items[0].autor.id", notNullValue());
    }

    @Test
    void filtroDeEstoqueNaListagemDeProdutos() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado zerado = Fixtures.criarDrone(a, 100.0, 0);
        Fixtures.ProdutoCriado cheio = Fixtures.criarDrone(a, 100.0, 50);
        given().queryParam("idMarca", a.idMarca()).queryParam("estoque", "ZERADO").when().get("/produtos")
                .then().statusCode(200)
                .body("items.id", hasItem((int) zerado.id()))
                .body("items.id", not(hasItem((int) cheio.id())));
        // estoque mínimo da fixture é 1: o zerado está abaixo dele
        given().queryParam("idMarca", a.idMarca()).queryParam("estoque", "ABAIXO_MINIMO").when().get("/produtos")
                .then().statusCode(200).body("totalItems", equalTo(1));
        given().queryParam("estoque", "QUALQUER").when().get("/produtos")
                .then().statusCode(400).body("errors.field", hasItem("estoque"));
    }

    @Test
    void visaoDeEstoqueFiltraPorMarca() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado p = Fixtures.criarAcessorio(a, 100.0, 3);
        Fixtures.criarAcessorio(100.0, 3); // outra marca
        given().queryParam("idMarca", a.idMarca()).when().get("/estoque")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].produto.id", equalTo((int) p.id()));
    }

    @Test
    void rotaDeVariantesSaiu() {
        given().when().get("/variantes").then().statusCode(404);
        given().when().get("/variantes/{id}", 1).then().statusCode(404);
    }
}

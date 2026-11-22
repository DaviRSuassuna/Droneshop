package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #21 Storefront: listagem, filtros e página de produto (RF06, RN17).
 * Contrato: docs/issues/021.md. Cada teste filtra pela própria marca para não
 * depender do resto do catálogo.
 */
@QuarkusTest
@Tag("issue-21")
class LojaProdutoResourceTest {

    static Fixtures.ProdutoCriado rascunho(Fixtures.Arvore a) {
        String s = Dados.sufixo();
        Map<String, Object> d = Fixtures.drone("Rascunho " + s, a,
                Fixtures.dadosComerciais("RS-" + s.toUpperCase(), 100.0, 1));
        d.put("idStatus", Fixtures.PRODUTO_RASCUNHO);
        io.restassured.path.json.JsonPath j = given().contentType(ContentType.JSON).body(d).when().post("/drones")
                .then().statusCode(200).extract().jsonPath();
        return new Fixtures.ProdutoCriado(j.getLong("id"), j.getString("slug"), j.getString("sku"));
    }

    /** Grupo ativo e uma versão dele (drone ATIVO) com o rótulo e o preço informados. */
    static long grupo() {
        String s = Dados.sufixo();
        return Fixtures.criar("/grupos-produto", Map.of("nome", "Grupo " + s, "ativo", true));
    }

    static Fixtures.ProdutoCriado versao(Fixtures.Arvore a, long idGrupo, String rotulo, double preco, long idStatus) {
        String s = Dados.sufixo();
        Map<String, Object> d = Fixtures.drone("Versao " + s, a, Fixtures.dadosComerciais("VS-" + s.toUpperCase(), preco, 2));
        d.put("idGrupo", idGrupo);
        d.put("rotuloNoGrupo", rotulo);
        d.put("idStatus", idStatus);
        io.restassured.path.json.JsonPath j = given().contentType(ContentType.JSON).body(d).when().post("/drones")
                .then().statusCode(200).extract().jsonPath();
        return new Fixtures.ProdutoCriado(j.getLong("id"), j.getString("slug"), j.getString("sku"));
    }

    @Test
    void soProdutoAtivoAparece() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.ProdutoCriado ativo = Fixtures.criarDrone(a, 5000.0, 2);
        Fixtures.ProdutoCriado rascunho = rascunho(a);
        anonimo().queryParam("marca", a.slugMarca()).when().get("/loja/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) ativo.id()))
                .body("items.id", not(hasItem((int) rascunho.id())));
        anonimo().when().get("/loja/produtos/{slug}", rascunho.slug()).then().statusCode(404);
    }

    @Test
    void filtroDePrecoUsaOPrecoDoProduto() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.criarDrone(a, 1000.0, 2);
        Fixtures.ProdutoCriado caro = Fixtures.criarDrone(a, 9000.0, 2);
        anonimo().queryParam("marca", a.slugMarca()).queryParam("precoMin", 5000).when().get("/loja/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) caro.id()))
                .body("items[0].precoMinimo", equalTo(9000.0f));
    }

    @Test
    void filtraPorTipoCategoriaEOrdena() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.criarDrone(a, 3000.0, 2);
        Fixtures.criarDrone(a, 2000.0, 2);
        Fixtures.criarAcessorio(a, 100.0, 2);
        anonimo().queryParam("categoria", a.slugCategoria()).queryParam("tipo", "DRONE").queryParam("ordem", "preco")
                .when().get("/loja/produtos")
                .then().statusCode(200)
                .body("items", hasSize(2))
                .body("items.precoMinimo", contains(2000.0f, 3000.0f));
    }

    @Test
    void filtraPorSpecDoDrone() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.criarDrone(a, 3000.0, 2); // autonomia 46 na fixture
        anonimo().queryParam("marca", a.slugMarca()).queryParam("autonomiaMin", 50).when().get("/loja/produtos")
                .then().statusCode(200).body("totalItems", equalTo(0));
        anonimo().queryParam("marca", a.slugMarca()).queryParam("autonomiaMin", 40).when().get("/loja/produtos")
                .then().statusCode(200).body("totalItems", equalTo(1));
    }

    @Test
    void buscaPorNomeIgnoraMaiusculasEAcentos() {
        Fixtures.Arvore a = Fixtures.arvore();
        String s = Dados.sufixo();
        Map<String, Object> d = Fixtures.drone("Águia " + s, a,
                Fixtures.dadosComerciais("AG-" + s.toUpperCase(), 100.0, 1));
        Fixtures.criar("/drones", d);
        anonimo().queryParam("nome", "AGUIA " + s.toUpperCase()).when().get("/loja/produtos")
                .then().statusCode(200).body("totalItems", equalTo(1));
    }

    @Test
    void paginaDoProdutoPorSlug() {
        Fixtures.ProdutoCriado p = Fixtures.criarDrone(4500.0, 3);
        anonimo().when().get("/loja/produtos/{slug}", p.slug())
                .then().statusCode(200)
                .body("id", equalTo((int) p.id()))
                .body("tipo.nome", equalTo("DRONE"))
                .body("autonomiaMinutos", equalTo(46))
                .body("sku", equalTo(p.sku()))
                .body("preco", equalTo(4500.0f))
                .body("disponivel", equalTo(3))
                .body("custoAquisicao", org.hamcrest.Matchers.nullValue())
                .body("grupo", org.hamcrest.Matchers.nullValue());
        anonimo().when().get("/loja/produtos/{slug}", "nao-existe-" + Dados.sufixo()).then().statusCode(404);
    }

    @Test
    void grupoViraUmItemSoAPartirDoMenorPrecoAtivo() {
        Fixtures.Arvore a = Fixtures.arvore();
        long idGrupo = grupo();
        versao(a, idGrupo, "Pro", 9000.0, Fixtures.PRODUTO_ATIVO);
        versao(a, idGrupo, "Standard", 6000.0, Fixtures.PRODUTO_ATIVO);
        versao(a, idGrupo, "Lite", 3000.0, Fixtures.PRODUTO_RASCUNHO);
        anonimo().queryParam("marca", a.slugMarca()).when().get("/loja/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].precoMinimo", equalTo(6000.0f));
    }

    @Test
    void paginaDaVersaoTrazAsOutrasVersoesAtivasDoGrupo() {
        Fixtures.Arvore a = Fixtures.arvore();
        long idGrupo = grupo();
        Fixtures.ProdutoCriado pro = versao(a, idGrupo, "Pro", 9000.0, Fixtures.PRODUTO_ATIVO);
        Fixtures.ProdutoCriado standard = versao(a, idGrupo, "Standard", 6000.0, Fixtures.PRODUTO_ATIVO);
        Fixtures.ProdutoCriado lite = versao(a, idGrupo, "Lite", 3000.0, Fixtures.PRODUTO_RASCUNHO);
        anonimo().when().get("/loja/produtos/{slug}", pro.slug())
                .then().statusCode(200)
                .body("grupo.versoes", hasSize(2))
                .body("grupo.versoes.id", hasItem((int) standard.id()))
                .body("grupo.versoes.id", not(hasItem((int) lite.id())))
                .body("grupo.versoes.find { it.id == " + standard.id() + " }.rotulo", equalTo("Standard"))
                .body("grupo.versoes.find { it.id == " + standard.id() + " }.preco", equalTo(6000.0f))
                .body("grupo.versoes.find { it.id == " + standard.id() + " }.disponivel", equalTo(2));
    }

    @Test
    void categoriasMarcasEFacetas() {
        Fixtures.Arvore a = Fixtures.arvore();
        Fixtures.criarDrone(a, 1000.0, 1);
        Fixtures.criarDrone(a, 3000.0, 1);
        anonimo().when().get("/loja/categorias").then().statusCode(200)
                .body("slug", hasItem(a.slugCategoria()));
        anonimo().queryParam("destaque", true).when().get("/loja/marcas").then().statusCode(200)
                .body("slug", hasItem(a.slugMarca()));
        anonimo().queryParam("categoria", a.slugCategoria()).when().get("/loja/filtros")
                .then().statusCode(200)
                .body("marcas.find { it.slug == '" + a.slugMarca() + "' }.quantidade", equalTo(2))
                .body("preco.min", equalTo(1000.0f))
                .body("preco.max", equalTo(3000.0f));
    }

    @Test
    void cardDoGrupoTrazAsVersoesEFiltrosContamSoOTipoPedido() {
        Fixtures.Arvore a = Fixtures.arvore();
        long idGrupo = grupo();
        versao(a, idGrupo, "Pro", 9000.0, Fixtures.PRODUTO_ATIVO);
        versao(a, idGrupo, "Standard", 7000.0, Fixtures.PRODUTO_ATIVO);
        Fixtures.criarAcessorio(a, 100.0, 2);
        anonimo().queryParam("marca", a.slugMarca()).queryParam("tipo", "DRONE").when().get("/loja/produtos")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].versoes", containsInAnyOrder("Pro", "Standard"))
                .body("items[0].precoMinimo", equalTo(7000.0f))
                .body("items[0].precoDe", equalTo(null));
        anonimo().queryParam("categoria", a.slugCategoria()).queryParam("tipo", "DRONE").when().get("/loja/filtros")
                .then().statusCode(200)
                .body("marcas.find { it.slug == '" + a.slugMarca() + "' }.quantidade", equalTo(1));
    }
}

package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

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
 * #80 GrupoProduto (D40): junta versões parecidas numa página de venda, com rótulo
 * obrigatório e único dentro do grupo. Excluir grupo com produtos responde 409 (D24).
 * Contrato: docs/issues/080.md
 */
@QuarkusTest
@Tag("issue-80")
class GrupoProdutoResourceTest {

    static Map<String, Object> grupo(String nome) {
        Map<String, Object> g = new HashMap<>();
        g.put("nome", nome);
        g.put("descricaoCurta", "Grupo criado pelo teste.");
        g.put("ativo", true);
        return g;
    }

    static ValidatableResponse post(Map<String, Object> g) {
        return given().contentType(ContentType.JSON).body(g).when().post("/grupos-produto").then();
    }

    static long criarGrupo() {
        return post(grupo("Grupo " + Dados.sufixo())).statusCode(200).extract().jsonPath().getLong("id");
    }

    /** Corpo de drone ATIVO na versão informada do grupo. */
    static Map<String, Object> versao(Fixtures.Arvore a, Long idGrupo, String rotulo, double preco) {
        Map<String, Object> d = Fixtures.drone("Versao " + Dados.sufixo(), a,
                Fixtures.dadosComerciais("VS-" + Dados.sufixo().toUpperCase(), preco, 2));
        d.put("idGrupo", idGrupo);
        d.put("rotuloNoGrupo", rotulo);
        return d;
    }

    static ValidatableResponse postDrone(Map<String, Object> d) {
        return given().contentType(ContentType.JSON).body(d).when().post("/drones").then();
    }

    @Test
    void crudCompletoComSlugDoNome() {
        String s = Dados.sufixo();
        long id = post(grupo("Falcon " + s)).statusCode(200)
                .body("slug", equalTo("falcon-" + s.toLowerCase()))
                .body("ativo", equalTo(true))
                .body("versoes", hasSize(0))
                .extract().jsonPath().getLong("id");

        Map<String, Object> alterado = grupo("Falcon alterado " + s);
        alterado.put("ativo", false);
        given().contentType(ContentType.JSON).body(alterado).when().put("/grupos-produto/{id}", id)
                .then().statusCode(204);
        given().when().get("/grupos-produto/{id}", id).then().statusCode(200)
                .body("nome", equalTo("Falcon alterado " + s))
                .body("ativo", equalTo(false));

        given().queryParam("nome", "alterado " + s).when().get("/grupos-produto")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) id));

        given().when().delete("/grupos-produto/{id}", id).then().statusCode(204);
        given().when().get("/grupos-produto/{id}", id).then().statusCode(404);
    }

    @Test
    void listagemFiltraPorAtivo() {
        String s = Dados.sufixo();
        long ativo = post(grupo("Filtro " + s + " A")).statusCode(200).extract().jsonPath().getLong("id");
        Map<String, Object> g = grupo("Filtro " + s + " B");
        g.put("ativo", false);
        long inativo = post(g).statusCode(200).extract().jsonPath().getLong("id");
        given().queryParam("nome", "filtro " + s).queryParam("ativo", false).when().get("/grupos-produto")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) inativo));
        given().queryParam("nome", "filtro " + s).queryParam("ativo", true).when().get("/grupos-produto")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) ativo));
    }

    @Test
    void nomeObrigatorioESlugUnico() {
        post(grupo("")).statusCode(400).body("errors.field", hasItem("nome"));
        String nome = "Unico " + Dados.sufixo();
        post(grupo(nome)).statusCode(200);
        post(grupo(nome)).statusCode(400).body("errors.field", hasItem("slug"));
    }

    @Test
    void produtoComGrupoExigeRotulo() {
        long idGrupo = criarGrupo();
        postDrone(versao(Fixtures.arvore(), idGrupo, null, 1000.0)).statusCode(400)
                .body("errors.field", hasItem("rotuloNoGrupo"));
        postDrone(versao(Fixtures.arvore(), Dados.ID_INEXISTENTE, "Pro", 1000.0)).statusCode(400)
                .body("errors.field", hasItem("idGrupo"));
    }

    @Test
    void rotuloEUnicoDentroDoGrupo() {
        Fixtures.Arvore a = Fixtures.arvore();
        long idGrupo = criarGrupo();
        postDrone(versao(a, idGrupo, "Pro", 1000.0)).statusCode(200)
                .body("grupo.id", equalTo((int) idGrupo))
                .body("rotuloNoGrupo", equalTo("Pro"));
        postDrone(versao(a, idGrupo, "pro", 2000.0)).statusCode(400)
                .body("errors.field", hasItem("rotuloNoGrupo"));
        // o mesmo rótulo em outro grupo pode
        postDrone(versao(a, criarGrupo(), "Pro", 2000.0)).statusCode(200);
    }

    @Test
    void grupoMisturaTiposEMostraAPartirDoMenorPrecoAtivo() {
        Fixtures.Arvore a = Fixtures.arvore();
        long idGrupo = criarGrupo();
        Map<String, Object> pro = versao(a, idGrupo, "Pro", 9000.0);
        pro.put("ordemNoGrupo", 1);
        long idPro = postDrone(pro).statusCode(200).body("grupo.nome", notBlank()).extract().jsonPath().getLong("id");

        Map<String, Object> kit = Fixtures.kit("Combo " + Dados.sufixo(), a, Fixtures.lista(Fixtures.componente(idPro, 1)));
        kit.put("preco", 7000.0);
        kit.put("idGrupo", idGrupo);
        kit.put("rotuloNoGrupo", "Combo");
        kit.put("ordemNoGrupo", 2);
        Fixtures.criar("/kits", kit);

        Map<String, Object> rascunho = versao(a, idGrupo, "Lite", 1000.0);
        rascunho.put("idStatus", Fixtures.PRODUTO_RASCUNHO);
        rascunho.put("ordemNoGrupo", 3);
        postDrone(rascunho).statusCode(200);

        given().when().get("/grupos-produto/{id}", idGrupo).then().statusCode(200)
                .body("versoes", hasSize(3))
                .body("versoes.rotulo", contains("Pro", "Combo", "Lite"))
                .body("versoes.tipo.nome", contains("DRONE", "KIT", "DRONE"))
                .body("precoAPartirDe", equalTo(7000.0f));

        given().queryParam("idGrupo", idGrupo).when().get("/produtos")
                .then().statusCode(200).body("totalItems", equalTo(3));
    }

    @Test
    void grupoComProdutoNaoSeExclui() {
        long idGrupo = criarGrupo();
        long idProduto = postDrone(versao(Fixtures.arvore(), idGrupo, "Pro", 1000.0)).statusCode(200)
                .extract().jsonPath().getLong("id");
        given().when().delete("/grupos-produto/{id}", idGrupo).then().statusCode(409);

        // tirando o produto do grupo, a exclusão passa
        Map<String, Object> corpo = Fixtures.corpoDoProduto(idProduto);
        corpo.put("idGrupo", null);
        Fixtures.alterarProduto(idProduto, corpo).then().statusCode(204);
        given().when().get("/produtos/{id}", idProduto).then().statusCode(200)
                .body("grupo", org.hamcrest.Matchers.nullValue())
                .body("rotuloNoGrupo", org.hamcrest.Matchers.nullValue());
        given().when().delete("/grupos-produto/{id}", idGrupo).then().statusCode(204);
    }

    @Test
    void inexistenteResponde404() {
        given().when().get("/grupos-produto/{id}", Dados.ID_INEXISTENTE).then().statusCode(404);
        given().contentType(ContentType.JSON).body(grupo("Fantasma " + Dados.sufixo()))
                .when().put("/grupos-produto/{id}", Dados.ID_INEXISTENTE).then().statusCode(404);
        given().when().delete("/grupos-produto/{id}", Dados.ID_INEXISTENTE).then().statusCode(404);
    }

    private static org.hamcrest.Matcher<String> notBlank() {
        return org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankOrNullString());
    }
}

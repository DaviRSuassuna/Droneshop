package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #80 Kit: terceiro tipo de produto (D40), sem estoque próprio, composto por outros
 * produtos (D7, D16). Substitui o kit-variante da #10.
 * Contrato: docs/issues/080.md
 */
@QuarkusTest
@Tag("issue-80")
class KitResourceTest {

    static Map<String, Object> kit(List<Map<String, Object>> componentes) {
        return Fixtures.kit("Kit " + Dados.sufixo(), Fixtures.arvore(), componentes);
    }

    static ValidatableResponse postKit(Map<String, Object> kit) {
        return given().contentType(ContentType.JSON).body(kit).when().post("/kits").then();
    }

    @Test
    void kitDerivaDisponibilidadeEPeso() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        long idBateria = Fixtures.criarAcessorio(499.0, 7).id();
        postKit(kit(List.of(Fixtures.componente(idDrone, 1), Fixtures.componente(idBateria, 2)))).statusCode(200)
                .body("tipo.nome", equalTo("KIT"))
                .body("estoqueFisico", nullValue())
                .body("disponivel", equalTo(3)) // min(10/1, 7/2)
                .body("componentes", hasSize(2))
                .body("componentes.idProduto", hasItems((int) idDrone, (int) idBateria))
                .body("pesoEmbalagemKg", equalTo(4.5f)) // 1,5 + 2 x 1,5
                .body("comprimentoEmbalagemCm", equalTo(75)) // 25 + 2 x 25
                .body("pesoSobrescrito", equalTo(false));
    }

    @Test
    void pesoInformadoSobrescreveODerivado() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        Map<String, Object> k = kit(List.of(Fixtures.componente(idDrone, 1)));
        k.put("pesoEmbalagemKg", 3.0);
        postKit(k).statusCode(200)
                .body("pesoEmbalagemKg", equalTo(3.0f))
                .body("pesoSobrescrito", equalTo(true));
    }

    @Test
    void kitComEstoqueResponde400() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        Map<String, Object> k = kit(List.of(Fixtures.componente(idDrone, 1)));
        k.put("estoqueFisico", 5);
        postKit(k).statusCode(400).body("errors.field", hasItem("estoqueFisico"));
    }

    @Test
    void kitAtivoSemComponentesResponde400() {
        postKit(kit(List.of())).statusCode(400).body("errors.field", hasItem("componentes"));
    }

    @Test
    void rascunhoAceitaKitSemComponentes() {
        Map<String, Object> k = kit(List.of());
        k.put("idStatus", Fixtures.PRODUTO_RASCUNHO);
        postKit(k).statusCode(200).body("disponivel", equalTo(0));
    }

    @Test
    void componenteNuncaEKitNemRepetido() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        long idKit = Fixtures.criarKit(Fixtures.componente(idDrone, 1)).id();
        postKit(kit(List.of(Fixtures.componente(idDrone, 1), Fixtures.componente(idKit, 1)))).statusCode(400)
                .body("errors.field", hasItem("componentes[1].idProduto"));
        postKit(kit(List.of(Fixtures.componente(idDrone, 1), Fixtures.componente(idDrone, 2)))).statusCode(400)
                .body("errors.field", hasItem("componentes[1].idProduto"));
    }

    @Test
    void componenteInexistenteOuQuantidadeZeroResponde400() {
        postKit(kit(List.of(Fixtures.componente(Dados.ID_INEXISTENTE, 1)))).statusCode(400)
                .body("errors.field", hasItem("componentes[0].idProduto"));
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        postKit(kit(List.of(Fixtures.componente(idDrone, 0)))).statusCode(400)
                .body("errors.field", hasItem("componentes[0].quantidade"));
    }

    @Test
    void putAlteraOsComponentes() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        long idBateria = Fixtures.criarAcessorio(499.0, 7).id();
        long idKit = Fixtures.criarKit(Fixtures.componente(idDrone, 1)).id();

        Map<String, Object> corpo = Fixtures.corpoDoProduto(idKit);
        corpo.put("componentes", List.of(Fixtures.componente(idDrone, 2), Fixtures.componente(idBateria, 1)));
        Fixtures.alterarProduto(idKit, corpo).then().statusCode(204);
        given().when().get("/produtos/{id}", idKit).then().statusCode(200)
                .body("componentes", hasSize(2))
                .body("componentes.find { it.idProduto == " + idDrone + " }.quantidade", equalTo(2))
                .body("disponivel", equalTo(5)); // min(10/2, 7/1)
    }

    @Test
    void componenteForaDoAtivoZeraADisponibilidade() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        long idKit = Fixtures.criarKit(Fixtures.componente(idDrone, 1)).id();
        Map<String, Object> drone = Fixtures.corpoDoProduto(idDrone);
        drone.put("idStatus", Fixtures.PRODUTO_INATIVO);
        Fixtures.alterarProduto(idDrone, drone).then().statusCode(204);
        given().when().get("/produtos/{id}", idKit).then().statusCode(200).body("disponivel", equalTo(0));
    }

    @Test
    void produtoQueEComponenteNaoSeExclui() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        long idKit = Fixtures.criarKit(Fixtures.componente(idDrone, 1)).id();
        given().when().delete("/produtos/{id}", idDrone).then().statusCode(409);
        given().when().delete("/produtos/{id}", idKit).then().statusCode(204);
        given().when().delete("/produtos/{id}", idDrone).then().statusCode(204);
    }

    @Test
    void kitTemRotaPropriaETipoKit() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        Fixtures.ProdutoCriado kit = Fixtures.criarKit(Fixtures.componente(idDrone, 1));
        given().contentType(ContentType.JSON).body(Fixtures.corpoDoProduto(kit.id()))
                .when().put("/drones/{id}", kit.id()).then().statusCode(404);
        given().when().get("/tipos-produto").then().statusCode(200)
                .body("nome", hasItems("DRONE", "ACESSORIO", "KIT"));
        given().queryParam("idTipo", Fixtures.TIPO_KIT).queryParam("busca", kit.sku())
                .when().get("/produtos").then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].id", equalTo((int) kit.id()));
    }

    @Test
    void kitEntraEmSubcategoriaDeQualquerTipoMasCategoriaNaoEKit() {
        Fixtures.Arvore a = Fixtures.arvore();
        long idDrone = Fixtures.criarDrone(a, 7999.0, 10).id();
        Map<String, Object> k = Fixtures.kit("Kit " + Dados.sufixo(), a, List.of(Fixtures.componente(idDrone, 1)));
        k.put("idSubcategoria", a.idSubcategoriaAcessorio());
        postKit(k).statusCode(200);

        Map<String, Object> categoria = new java.util.HashMap<>();
        categoria.put("nome", "Kits " + Dados.sufixo());
        categoria.put("ordemExibicao", 1);
        categoria.put("idStatus", Dados.ATIVA);
        categoria.put("idTipo", Fixtures.TIPO_KIT);
        given().contentType(ContentType.JSON).body(categoria).when().post("/categorias")
                .then().statusCode(400).body("errors.field", hasItem("idTipo"));
    }

    @Test
    void kitNaoRecebeMovimentoDeEstoque() {
        long idDrone = Fixtures.criarDrone(7999.0, 10).id();
        long idKit = Fixtures.criarKit(Fixtures.componente(idDrone, 1)).id();
        given().contentType(ContentType.JSON)
                .body(Map.of("idProduto", idKit, "idTipo", Fixtures.MOV_ENTRADA, "quantidade", 1))
                .when().post("/estoque/movimentos").then().statusCode(400)
                .body("errors.field", hasItem("idProduto"));
    }
}

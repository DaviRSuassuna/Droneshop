package br.com.droneshop.compra;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.como;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Issue nova "Favoritos: queda de preço, mover tudo para o carrinho e lista compartilhada".
 * Contrato: docs/issues/nova-favoritos-compartilhar.md. A etiqueta é o nome do arquivo.
 */
@QuarkusTest
@Tag("issue-favoritos-compartilhar")
class FavoritosCompartilharResourceTest {

    static void favoritar(Fixtures.Cliente c, long idProduto) {
        como(c.token()).contentType(ContentType.JSON).body(Map.of("idProduto", idProduto))
                .when().post("/conta/desejos").then().statusCode(204);
    }

    static String compartilhar(Fixtures.Cliente c) {
        return como(c.token()).when().post("/conta/desejos/compartilhar")
                .then().statusCode(200)
                .body("url", startsWith("/lista/"))
                .extract().jsonPath().getString("token");
    }

    @Test
    void avisaQuandoOPrecoCaiDepoisDeFavoritar() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        Fixtures.ProdutoCriado p = Fixtures.criarAcessorio(500.0, 3);
        favoritar(c, p.id());
        como(c.token()).when().get("/conta/desejos")
                .then().statusCode(200)
                .body("[0].precoAoAdicionar", equalTo(500.0f))
                .body("[0].quedaDePreco", equalTo(false));

        Map<String, Object> corpo = Fixtures.corpoDoProduto(p.id());
        corpo.put("preco", 450.0);
        Fixtures.alterarProduto(p.id(), corpo).then().statusCode(204);
        como(c.token()).when().get("/conta/desejos")
                .then().statusCode(200)
                .body("[0].quedaDePreco", equalTo(true));
    }

    @Test
    void moveODisponivelParaOCarrinhoEInformaOResto() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long disponivel = Fixtures.criarAcessorio(200.0, 3).id();
        long semEstoque = Fixtures.criarAcessorio(200.0, 0).id();
        favoritar(c, disponivel);
        favoritar(c, semEstoque);

        como(c.token()).when().post("/conta/desejos/mover-para-carrinho")
                .then().statusCode(200)
                .body("carrinho.itens.produto.id", hasItem((int) disponivel))
                .body("carrinho.itens.produto.id", not(hasItem((int) semEstoque)))
                .body("naoAdicionados.idProduto", hasItem((int) semEstoque));
        como(c.token()).when().get("/conta/desejos")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].produto.id", equalTo((int) semEstoque));
    }

    @Test
    void listaCompartilhadaEPublicaEMostraSoOPrimeiroNome() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long p = Fixtures.criarAcessorio(200.0, 3).id();
        favoritar(c, p);
        String token = compartilhar(c);
        anonimo().when().get("/loja/listas/{token}", token)
                .then().statusCode(200)
                .body("nomeCliente", equalTo("Cliente"))
                .body("itens.produto.id", hasItem((int) p));
    }

    @Test
    void compartilharDeNovoDevolveOMesmoToken() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        favoritar(c, Fixtures.criarAcessorio(200.0, 3).id());
        String primeiro = compartilhar(c);
        assertEquals(primeiro, compartilhar(c));
    }

    @Test
    void desligarOLinkInvalidaOToken() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        favoritar(c, Fixtures.criarAcessorio(200.0, 3).id());
        String token = compartilhar(c);
        como(c.token()).when().delete("/conta/desejos/compartilhar").then().statusCode(204);
        anonimo().when().get("/loja/listas/{token}", token).then().statusCode(404);
    }

    @Test
    void semTokenDa401() {
        anonimo().when().post("/conta/desejos/compartilhar").then().statusCode(401);
        anonimo().when().post("/conta/desejos/mover-para-carrinho").then().statusCode(401);
    }
}

package br.com.droneshop.pedido;

import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;

/**
 * #35 NF-e simulada de venda e de devolução (RF29, D3).
 * Contrato: docs/issues/035.md
 */
@QuarkusTest
@Tag("issue-35")
class NotaFiscalResourceTest {

    /** Dígito verificador da chave de acesso da NF-e (módulo 11, pesos 2 a 9 da direita). */
    static int dvChave(String chave43) {
        int soma = 0, peso = 2;
        for (int i = chave43.length() - 1; i >= 0; i--) {
            soma += (chave43.charAt(i) - '0') * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    @Test
    void listaTiposDeOrigem() {
        given().when().get("/tipos-origem-nota").then().statusCode(200).body("nome", contains("VENDA", "DEVOLUCAO"));
    }

    @Test
    void emiteNotaDeVendaComChaveValidaEArquivos() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pedido = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1);
        JsonPath nota = given().when().post("/pedidos/{id}/nota-fiscal", pedido)
                .then().statusCode(200)
                .body("tipoOrigem.nome", equalTo("VENDA"))
                .body("serie", equalTo("1"))
                .body("chaveAcesso", matchesPattern("\\d{44}"))
                .extract().jsonPath();
        String chave = nota.getString("chaveAcesso");
        org.junit.jupiter.api.Assertions.assertEquals(dvChave(chave.substring(0, 43)), chave.charAt(43) - '0');
        given().when().get(nota.getString("xmlUrl")).then().statusCode(200).contentType("application/xml");
        given().when().get(nota.getString("danfeUrl")).then().statusCode(200).contentType("application/pdf");
        como(c.token()).when().get("/conta/pedidos/{id}/nota-fiscal", pedido).then().statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    void numeroSequencial() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long p1 = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1);
        long p2 = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1);
        long n1 = given().when().post("/pedidos/{id}/nota-fiscal", p1).then().statusCode(200)
                .extract().jsonPath().getLong("numero");
        long n2 = given().when().post("/pedidos/{id}/nota-fiscal", p2).then().statusCode(200)
                .extract().jsonPath().getLong("numero");
        org.junit.jupiter.api.Assertions.assertEquals(n1 + 1, n2);
    }

    @Test
    void pedidoPendenteOuComNotaResponde409() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        long pendente = Fixtures.pedidoPendente(c, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1, Fixtures.PIX);
        given().when().post("/pedidos/{id}/nota-fiscal", pendente).then().statusCode(409);
        long pago = Fixtures.pedidoPago(c, Fixtures.criarAcessorio(100.0, 5).idVariante(), 1);
        given().when().post("/pedidos/{id}/nota-fiscal", pago).then().statusCode(200);
        given().when().post("/pedidos/{id}/nota-fiscal", pago).then().statusCode(409);
    }
}

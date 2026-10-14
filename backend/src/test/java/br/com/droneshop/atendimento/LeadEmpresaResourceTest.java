package br.com.droneshop.atendimento;

import static br.com.droneshop.testes.Api.anonimo;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Dados;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Issue nova "Proposta comercial para empresas" (D36).
 * Contrato: docs/issues/nova-empresas.md. A etiqueta é o nome do arquivo.
 */
@QuarkusTest
@Tag("issue-empresas")
class LeadEmpresaResourceTest {

    static Map<String, Object> lead(String razao) {
        Map<String, Object> l = new HashMap<>();
        l.put("razaoSocial", razao);
        l.put("cnpj", Dados.cnpjValido());
        l.put("nomeContato", "Marina");
        l.put("emailContato", "marina@empresa.com.br");
        l.put("telefoneContato", "63999990000");
        l.put("segmento", "Agronegócio");
        l.put("quantidadeEstimada", 5);
        l.put("mensagem", "Queremos uma proposta para mapeamento.");
        return l;
    }

    @Test
    void enviaPropostaSemLoginEOAdminVe() {
        String razao = "Fazenda " + Dados.sufixo() + " Ltda.";
        anonimo().contentType(ContentType.JSON).body(lead(razao)).when().post("/leads-empresas")
                .then().statusCode(204);
        given().queryParam("pageSize", 1000).when().get("/admin/leads-empresas")
                .then().statusCode(200)
                .body("items.find { it.razaoSocial == '" + razao + "' }.status.nome", equalTo("NOVO"));
    }

    @Test
    void cnpjEEmailValidados() {
        Map<String, Object> l = lead("Invalida " + Dados.sufixo());
        l.put("cnpj", "12345678000100");
        l.put("emailContato", "sem-arroba");
        anonimo().contentType(ContentType.JSON).body(l).when().post("/leads-empresas")
                .then().statusCode(400)
                .body("errors.field", hasItem("cnpj"))
                .body("errors.field", hasItem("emailContato"));
    }
}

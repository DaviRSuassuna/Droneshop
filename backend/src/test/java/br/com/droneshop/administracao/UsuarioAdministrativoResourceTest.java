package br.com.droneshop.administracao;

import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #5 PerfilAdministrativo com papéis e permissões por módulo.
 * Contrato: docs/issues/005.md
 */
@QuarkusTest
@Tag("issue-5")
class UsuarioAdministrativoResourceTest {

    static final long MODULO_CATALOGO = 1, MODULO_ESTOQUE = 2;

    static Map<String, Object> usuario(String email, long idPapel) {
        Map<String, Object> u = new HashMap<>();
        u.put("nome", "Usuario " + email);
        u.put("email", email);
        u.put("senha", "provisoria123");
        u.put("idPapel", idPapel);
        u.put("ativo", true);
        return u;
    }

    @Test
    void listaPapeisEModulos() {
        given().when().get("/papeis-administrativos")
                .then().statusCode(200)
                .body("$", hasSize(5))
                .body("nome", hasItems("ADMIN", "OPERADOR", "CATALOGO", "ATENDIMENTO", "SOMENTE_LEITURA"));
        given().when().get("/modulos-sistema")
                .then().statusCode(200)
                .body("$", hasSize(8));
    }

    @Test
    void permissoesPadraoDoPapelCatalogo() {
        given().when().get("/admin/papeis/{id}/permissoes-padrao", Fixtures.PAPEL_CATALOGO)
                .then().statusCode(200)
                .body("$", hasSize(8))
                .body("find { it.modulo.nome == 'CATALOGO' }.podeExcluir", equalTo(true))
                .body("find { it.modulo.nome == 'ESTOQUE' }.podeEditar", equalTo(true))
                .body("find { it.modulo.nome == 'PEDIDO' }.podeEditar", equalTo(false))
                .body("find { it.modulo.nome == 'PEDIDO' }.podeVer", equalTo(true));
    }

    @Test
    void criaUsuarioComPermissoesDoPapel() {
        String email = Dados.sufixo() + "@aeria.com.br";
        given().contentType(ContentType.JSON).body(usuario(email, Fixtures.PAPEL_SOMENTE_LEITURA))
                .when().post("/admin/usuarios")
                .then().statusCode(200)
                .body("email", equalTo(email))
                .body("papel.nome", equalTo("SOMENTE_LEITURA"))
                .body("permissoes", hasSize(8))
                .body("permissoes.podeEditar", not(hasItem(true)))
                .body("permissoes.podeVer", hasItem(true));
    }

    @Test
    void ajustePorUsuarioSobrepoeSoOModuloInformado() {
        Map<String, Object> u = usuario(Dados.sufixo() + "@aeria.com.br", Fixtures.PAPEL_SOMENTE_LEITURA);
        u.put("permissoes", List.of(Map.of("idModulo", MODULO_ESTOQUE, "podeVer", true, "podeEditar", true,
                "podeExcluir", false)));
        given().contentType(ContentType.JSON).body(u)
                .when().post("/admin/usuarios")
                .then().statusCode(200)
                .body("permissoes.find { it.modulo.nome == 'ESTOQUE' }.podeEditar", equalTo(true))
                .body("permissoes.find { it.modulo.nome == 'CATALOGO' }.podeEditar", equalTo(false));
    }

    @Test
    void emailRepetidoSenhaCurtaEPapelInexistenteRespondem400() {
        given().contentType(ContentType.JSON).body(usuario(Api.ADMIN_EMAIL, Fixtures.PAPEL_ADMIN))
                .when().post("/admin/usuarios")
                .then().statusCode(400).body("errors.field", hasItem("email"));

        Map<String, Object> curta = usuario(Dados.sufixo() + "@aeria.com.br", Fixtures.PAPEL_ADMIN);
        curta.put("senha", "123");
        given().contentType(ContentType.JSON).body(curta)
                .when().post("/admin/usuarios")
                .then().statusCode(400).body("errors.field", hasItem("senha"));

        given().contentType(ContentType.JSON).body(usuario(Dados.sufixo() + "@aeria.com.br", 99))
                .when().post("/admin/usuarios")
                .then().statusCode(400).body("errors.field", hasItem("idPapel"));
    }

    @Test
    void somenteLeituraListaMasNaoCria() {
        String token = Fixtures.adminComPapel(Fixtures.PAPEL_SOMENTE_LEITURA);
        como(token).when().get("/marcas").then().statusCode(200);
        como(token).contentType(ContentType.JSON).body(Map.of("idStatus", Dados.RASCUNHO))
                .when().post("/marcas")
                .then().statusCode(403);
    }

    @Test
    void catalogoNaoMexeEmUsuarios() {
        String token = Fixtures.adminComPapel(Fixtures.PAPEL_CATALOGO);
        como(token).contentType(ContentType.JSON)
                .body(usuario(Dados.sufixo() + "@aeria.com.br", Fixtures.PAPEL_ADMIN))
                .when().post("/admin/usuarios")
                .then().statusCode(403);
    }

    @Test
    void usuarioDesativadoNaoEntra() {
        String email = Dados.sufixo() + "@aeria.com.br";
        long id = Fixtures.criar("/admin/usuarios", usuario(email, Fixtures.PAPEL_OPERADOR));
        Map<String, Object> u = usuario(email, Fixtures.PAPEL_OPERADOR);
        u.put("ativo", false);
        u.remove("senha");
        given().contentType(ContentType.JSON).body(u)
                .when().put("/admin/usuarios/{id}", id)
                .then().statusCode(204);
        Api.tentarLogin(email, "provisoria123").then().statusCode(401);
    }

    @Test
    void ninguemDesativaOProprioUsuario() {
        long idAdmin = given().when().get("/auth/me").then().statusCode(200).extract().jsonPath().getLong("id");
        Map<String, Object> u = usuario(Api.ADMIN_EMAIL, Fixtures.PAPEL_ADMIN);
        u.put("ativo", false);
        u.remove("senha");
        given().contentType(ContentType.JSON).body(u)
                .when().put("/admin/usuarios/{id}", idAdmin)
                .then().statusCode(409);
    }

    @Test
    void consultaEBuscaUsuarios() {
        String s = Dados.sufixo();
        long id = Fixtures.criar("/admin/usuarios", usuario(s + "@aeria.com.br", Fixtures.PAPEL_ATENDIMENTO));
        given().when().get("/admin/usuarios/{id}", id)
                .then().statusCode(200).body("papel.nome", equalTo("ATENDIMENTO"));
        given().queryParam("nome", s).when().get("/admin/usuarios")
                .then().statusCode(200).body("totalItems", equalTo(1));
        given().when().get("/admin/usuarios/{id}", Dados.ID_INEXISTENTE).then().statusCode(404);
    }
}

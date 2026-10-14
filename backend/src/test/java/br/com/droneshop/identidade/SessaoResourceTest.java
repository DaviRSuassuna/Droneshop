package br.com.droneshop.identidade;

import static br.com.droneshop.testes.Api.como;
import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.testes.Api;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * #20 SessaoUsuario: escolha de perfil no login e revogação (RF48, RF50).
 * Contrato: docs/issues/020.md
 */
@QuarkusTest
@Tag("issue-20")
class SessaoResourceTest {

    static final long ADMINISTRATIVO = 2;

    /** Cliente que também ganhou perfil administrativo (o admin acrescenta pelo e-mail, #5). */
    static Fixtures.Cliente clienteQueTambemEAdmin() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        Map<String, Object> u = new HashMap<>();
        u.put("nome", "Cliente e admin");
        u.put("email", c.email());
        u.put("senha", "ignorada123");
        u.put("idPapel", Fixtures.PAPEL_CATALOGO);
        u.put("ativo", true);
        given().contentType(ContentType.JSON).body(u).when().post("/admin/usuarios").then().statusCode(200);
        return c;
    }

    @Test
    void doisPerfisPedemEscolha() {
        Fixtures.Cliente c = clienteQueTambemEAdmin();
        String parcial = Api.tentarLogin(c.email(), c.senha()).then().statusCode(200)
                .body("perfis", hasItems("CLIENTE", "ADMINISTRATIVO"))
                .body("perfilAtivo", nullValue())
                .body("escolherPerfil", equalTo(true))
                .extract().jsonPath().getString("token");
        como(parcial).when().get("/marcas").then().statusCode(403);
        como(parcial).when().get("/auth/me").then().statusCode(200);

        String admin = como(parcial).contentType(ContentType.JSON).body(Map.of("idPerfilAtivo", ADMINISTRATIVO))
                .when().post("/auth/perfil-ativo")
                .then().statusCode(200)
                .body("perfilAtivo", equalTo("ADMINISTRATIVO"))
                .extract().jsonPath().getString("token");
        como(admin).when().get("/marcas").then().statusCode(200);
        como(admin).when().get("/conta/perfil").then().statusCode(403);
    }

    @Test
    void umPerfilJaEntraAtivo() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        Api.tentarLogin(c.email(), c.senha()).then().statusCode(200)
                .body("perfilAtivo", equalTo("CLIENTE"))
                .body("escolherPerfil", equalTo(false));
    }

    @Test
    void perfilQueOUsuarioNaoTemResponde400() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).contentType(ContentType.JSON).body(Map.of("idPerfilAtivo", ADMINISTRATIVO))
                .when().post("/auth/perfil-ativo")
                .then().statusCode(400).body("errors.field", hasItem("idPerfilAtivo"));
    }

    @Test
    void revogarSessaoDerrubaOToken() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        String segundo = Api.login(c.email(), c.senha());
        long idPrimeira = como(segundo).when().get("/conta/sessoes")
                .then().statusCode(200)
                .body("$", hasSize(2))
                .extract().jsonPath().getLong("find { !it.atual }.id");
        como(segundo).when().delete("/conta/sessoes/{id}", idPrimeira).then().statusCode(204);
        como(c.token()).when().get("/conta/perfil").then().statusCode(401);
        como(segundo).when().get("/conta/perfil").then().statusCode(200);
    }

    @Test
    void logoutRevogaASessaoAtual() {
        Fixtures.Cliente c = Fixtures.novoCliente();
        como(c.token()).when().post("/auth/logout").then().statusCode(204);
        como(c.token()).when().get("/conta/perfil").then().statusCode(401);
    }

    @Test
    void sessaoDeOutroUsuarioResponde404() {
        Fixtures.Cliente dono = Fixtures.novoCliente();
        Fixtures.Cliente outro = Fixtures.novoCliente();
        long id = como(dono.token()).when().get("/conta/sessoes").then().statusCode(200)
                .extract().jsonPath().getLong("[0].id");
        como(outro.token()).when().delete("/conta/sessoes/{id}", id).then().statusCode(404);
    }

    @Test
    void listaOsPerfisAtivos() {
        Api.anonimo().when().get("/perfis-ativos").then().statusCode(200)
                .body("nome", hasItems("CLIENTE", "ADMINISTRATIVO"));
    }
}

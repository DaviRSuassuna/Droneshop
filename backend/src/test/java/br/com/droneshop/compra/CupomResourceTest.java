package br.com.droneshop.compra;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.TipoPessoa;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.testes.Dados;
import br.com.droneshop.compra.model.Cupom;
import br.com.droneshop.compra.model.UsoCupom;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #15 Cupom: cadastro com validade, limites e restrições (RF20, RN03).
 * Contrato: docs/issues/015.md
 */
@QuarkusTest
@Tag("issue-15")
public class CupomResourceTest {

    public static final long PERCENTUAL = 1, VALOR_FIXO = 2;

    @Inject
    EntityManager entityManager;

    static String dias(int d) {
        return LocalDateTime.now().plusDays(d).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    public static Map<String, Object> cupom(String codigo, long tipo, double valor) {
        Map<String, Object> c = new HashMap<>();
        c.put("codigo", codigo);
        c.put("idTipoDesconto", tipo);
        c.put("valor", valor);
        c.put("dataInicio", dias(-1));
        c.put("dataFim", dias(30));
        c.put("valorMinimo", 0);
        c.put("limiteTotalUso", 100);
        c.put("limitePorCliente", 1);
        c.put("idsCategorias", List.of());
        c.put("idsMarcas", List.of());
        c.put("idsVariantes", List.of());
        return c;
    }

    public static ValidatableResponse post(Map<String, Object> c) {
        return given().contentType(ContentType.JSON).body(c).when().post("/cupons").then();
    }

    @Test
    void listaOsTiposDeDesconto() {
        given().when().get("/tipos-desconto-cupom").then().statusCode(200)
                .body("nome", contains("PERCENTUAL", "VALOR_FIXO"));
    }

    @Test
    void codigoGuardadoEmMaiusculas() {
        String codigo = "bem" + Dados.sufixo();
        post(cupom(codigo, PERCENTUAL, 10)).statusCode(200)
                .body("codigo", equalTo(codigo.toUpperCase()))
                .body("situacao", equalTo("VIGENTE"))
                .body("usos", equalTo(0));
    }

    @Test
    void codigoRepetidoSemDiferenciarMaiusculas() {
        String codigo = "REP" + Dados.sufixo().toUpperCase();
        post(cupom(codigo, PERCENTUAL, 10)).statusCode(200);
        post(cupom(codigo.toLowerCase(), PERCENTUAL, 10)).statusCode(400)
                .body("errors.field", hasItem("codigo"));
        post(cupom("COM ESPACO", PERCENTUAL, 10)).statusCode(400)
                .body("errors.field", hasItem("codigo"));
    }

    @Test
    void validacoesDeValorDataELimite() {
        post(cupom("P" + Dados.sufixo(), PERCENTUAL, 101)).statusCode(400).body("errors.field", hasItem("valor"));
        post(cupom("V" + Dados.sufixo(), VALOR_FIXO, 0)).statusCode(400).body("errors.field", hasItem("valor"));

        Map<String, Object> datas = cupom("D" + Dados.sufixo(), PERCENTUAL, 10);
        datas.put("dataFim", dias(-5));
        post(datas).statusCode(400).body("errors.field", hasItem("dataFim"));

        Map<String, Object> limite = cupom("L" + Dados.sufixo(), PERCENTUAL, 10);
        limite.put("limiteTotalUso", 1);
        limite.put("limitePorCliente", 2);
        post(limite).statusCode(400).body("errors.field", hasItem("limitePorCliente"));
    }

    @Test
    void crudEUsos() {
        String codigo = "CRUD" + Dados.sufixo().toUpperCase();
        Map<String, Object> c = cupom(codigo, VALOR_FIXO, 50);
        long id = post(c).statusCode(200).extract().jsonPath().getLong("id");
        given().queryParam("codigo", codigo).when().get("/cupons")
                .then().statusCode(200).body("totalItems", equalTo(1));
        c.put("valor", 60);
        given().contentType(ContentType.JSON).body(c).when().put("/cupons/{id}", id).then().statusCode(204);
        given().when().get("/cupons/{id}", id).then().statusCode(200).body("valor", equalTo(60.0f));
        given().when().get("/cupons/{id}/usos", id).then().statusCode(200).body("totalItems", equalTo(0));
        given().when().delete("/cupons/{id}", id).then().statusCode(204);
        given().when().get("/cupons/{id}", id).then().statusCode(404);
    }

    @Test
    void cupomUsadoNaoPodeSerExcluido() {
        String codigo = "USADO" + Dados.sufixo().toUpperCase();
        long id = post(cupom(codigo, PERCENTUAL, 10)).statusCode(200).extract().jsonPath().getLong("id");
        String sufixo = Dados.sufixo();
        long[] ids = new long[4]; // uso, pedido, perfil, usuario

        QuarkusTransaction.requiringNew().run(() -> {
            Cupom cupom = entityManager.find(Cupom.class, id);
            Usuario usuario = new Usuario();
            usuario.setNome("Cliente " + sufixo);
            usuario.setEmail(sufixo + "@cliente.com.br");
            usuario.setEmailVerificado(true);
            usuario.setAtivo(true);
            entityManager.persist(usuario);

            PerfilCliente cliente = new PerfilCliente();
            cliente.setUsuario(usuario);
            cliente.setTipoPessoa(TipoPessoa.PF);
            cliente.setNome(usuario.getNome());
            cliente.setDocumento(br.com.droneshop.testes.Fixtures.cpfValido());
            entityManager.persist(cliente);

            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setStatus(StatusPedido.PENDENTE);
            pedido.setValorTotal(java.math.BigDecimal.ZERO);
            pedido.setValorFrete(java.math.BigDecimal.ZERO);
            pedido.setValorDesconto(java.math.BigDecimal.TEN);
            entityManager.persist(pedido);

            UsoCupom uso = new UsoCupom();
            uso.setCupom(cupom);
            uso.setCliente(cliente);
            uso.setPedido(pedido);
            entityManager.persist(uso);
            ids[0] = uso.getId();
            ids[1] = pedido.getId();
            ids[2] = cliente.getId();
            ids[3] = usuario.getId();
        });

        try {
            given().when().delete("/cupons/{id}", id).then().statusCode(409);
            given().when().get("/cupons/{id}/usos", id).then().statusCode(200)
                    .body("totalItems", equalTo(1))
                    .body("items[0].pedido.id", org.hamcrest.Matchers.notNullValue())
                    .body("items[0].cliente.nome", equalTo("Cliente " + sufixo));
        } finally {
            // O pedido sem itens não pode sobrar: Fixtures.pedidoDaCarga() pega o mais recente de cada status.
            QuarkusTransaction.requiringNew().run(() -> {
                entityManager.remove(entityManager.find(UsoCupom.class, ids[0]));
                entityManager.remove(entityManager.find(Pedido.class, ids[1]));
                entityManager.remove(entityManager.find(PerfilCliente.class, ids[2]));
                entityManager.remove(entityManager.find(Usuario.class, ids[3]));
            });
        }
    }
}

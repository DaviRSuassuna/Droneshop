package br.com.droneshop.catalogo;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.droneshop.catalogo.model.Promocao;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.pedido.model.DescontoPedido;
import br.com.droneshop.pedido.model.OrigemDesconto;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.testes.Dados;
import br.com.droneshop.testes.Fixtures;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * #14 Promoções com vigência e escopo (RF05).
 * Contrato: docs/issues/014.md
 */
@QuarkusTest
@Tag("issue-14")
class PromocaoResourceTest {

    static final long PRODUTO = 1, CARRINHO = 2, FRETE = 3;

    @Inject
    EntityManager entityManager;

    static String dias(int d) {
        return LocalDateTime.now().plusDays(d).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    static Map<String, Object> promocao(long idProduto, int inicio, int fim) {
        Map<String, Object> p = new HashMap<>();
        p.put("nome", "Promo " + Dados.sufixo());
        p.put("idEscopo", PRODUTO);
        p.put("dataInicio", dias(inicio));
        p.put("dataFim", dias(fim));
        p.put("percentualDesconto", 10);
        p.put("valorDesconto", null);
        p.put("valorMinimoCarrinho", null);
        p.put("ativa", true);
        p.put("idsProdutos", List.of(idProduto));
        return p;
    }

    static ValidatableResponse post(Map<String, Object> p) {
        return given().contentType(ContentType.JSON).body(p).when().post("/promocoes").then();
    }

    @Test
    void listaOsEscopos() {
        given().when().get("/escopos-promocao").then().statusCode(200)
                .body("nome", contains("PRODUTO", "CARRINHO", "FRETE"));
    }

    @Test
    void situacaoDerivadaDasDatas() {
        long v = Fixtures.criarDrone(1000.0, 5).id();
        post(promocao(v, 5, 10)).statusCode(200).body("situacao", equalTo("AGENDADA"))
                .body("produtos[0].id", equalTo((int) v));
        post(promocao(v, -1, 5)).statusCode(200).body("situacao", equalTo("VIGENTE"));
        post(promocao(v, -10, -5)).statusCode(200).body("situacao", equalTo("ENCERRADA"));
        Map<String, Object> inativa = promocao(v, -1, 5);
        inativa.put("ativa", false);
        post(inativa).statusCode(200).body("situacao", equalTo("INATIVA"));
    }

    @Test
    void percentualEValorSaoExclusivos() {
        long v = Fixtures.criarDrone(1000.0, 5).id();
        Map<String, Object> dois = promocao(v, -1, 5);
        dois.put("valorDesconto", 50);
        post(dois).statusCode(400).body("errors.field", hasItem("percentualDesconto"));

        Map<String, Object> nenhum = promocao(v, -1, 5);
        nenhum.put("percentualDesconto", null);
        post(nenhum).statusCode(400).body("errors.field", hasItem("percentualDesconto"));

        Map<String, Object> fora = promocao(v, -1, 5);
        fora.put("percentualDesconto", 120);
        post(fora).statusCode(400).body("errors.field", hasItem("percentualDesconto"));
    }

    @Test
    void validacoesDeDataEscopo() {
        long v = Fixtures.criarDrone(1000.0, 5).id();
        post(promocao(v, 5, 1)).statusCode(400).body("errors.field", hasItem("dataFim"));

        Map<String, Object> semProdutos = promocao(v, -1, 5);
        semProdutos.put("idsProdutos", List.of());
        post(semProdutos).statusCode(400).body("errors.field", hasItem("idsProdutos"));

        Map<String, Object> carrinho = promocao(v, -1, 5);
        carrinho.put("idEscopo", CARRINHO);
        carrinho.put("idsProdutos", List.of());
        post(carrinho).statusCode(400).body("errors.field", hasItem("valorMinimoCarrinho"));
    }

    @Test
    void filtraPorSituacao() {
        long v = Fixtures.criarDrone(1000.0, 5).id();
        long id = post(promocao(v, 5, 10)).statusCode(200).extract().jsonPath().getLong("id");
        given().queryParam("situacao", "AGENDADA").queryParam("pageSize", 1000).when().get("/promocoes")
                .then().statusCode(200).body("items.id", hasItem((int) id));
        given().queryParam("situacao", "VIGENTE").queryParam("pageSize", 1000).when().get("/promocoes")
                .then().statusCode(200).body("items.id", not(hasItem((int) id)));
    }

    @Test
    void crudCompleto() {
        long v = Fixtures.criarDrone(1000.0, 5).id();
        Map<String, Object> p = promocao(v, 1, 5);
        long id = post(p).statusCode(200).extract().jsonPath().getLong("id");
        p.put("nome", "Alterada");
        given().contentType(ContentType.JSON).body(p).when().put("/promocoes/{id}", id).then().statusCode(204);
        given().when().get("/promocoes/{id}", id).then().statusCode(200).body("nome", equalTo("Alterada"));
        given().when().delete("/promocoes/{id}", id).then().statusCode(204);
        given().when().get("/promocoes/{id}", id).then().statusCode(404);
    }

    @Test
    void promocaoAplicadaEmPedidoNaoPodeSerExcluida() {
        long produto = Fixtures.criarDrone(1000.0, 5).id();
        long idPromocao = post(promocao(produto, -1, 5)).statusCode(200).extract().jsonPath().getLong("id");
        var conta = Fixtures.novoCliente();
        long[] ids = new long[1];

        QuarkusTransaction.requiringNew().run(() -> {
            Promocao promocao = entityManager.find(Promocao.class, idPromocao);
            PerfilCliente cliente = entityManager.createQuery("from PerfilCliente where usuario.id = :id", PerfilCliente.class)
                    .setParameter("id", conta.id()).getSingleResult();

            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setStatus(StatusPedido.PENDENTE);
            pedido.setValorTotal(BigDecimal.ZERO);
            pedido.setValorFrete(BigDecimal.ZERO);
            pedido.setValorDesconto(BigDecimal.ZERO);
            DescontoPedido desconto = new DescontoPedido();
            desconto.setOrigem(OrigemDesconto.PROMOCAO);
            desconto.setDescricao(promocao.getNome());
            desconto.setValor(BigDecimal.TEN);
            desconto.setPedido(pedido);
            desconto.setPromocao(promocao);
            pedido.setDescontos(new ArrayList<>(List.of(desconto)));
            entityManager.persist(pedido);
            ids[0] = pedido.getId();
        });

        try {
            given().when().delete("/promocoes/{id}", idPromocao).then().statusCode(409);
        } finally {
            // O pedido sem itens não pode sobrar: Fixtures.pedidoDaCarga() pega o mais recente de cada status.
            QuarkusTransaction.requiringNew().run(() -> {
                entityManager.remove(entityManager.find(Pedido.class, ids[0])); // o desconto vai junto (cascade)
            });
        }
    }
}

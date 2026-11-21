package br.com.droneshop.administracao;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;

/**
 * Issue nova "Dashboard: comparação com o período anterior, vendas por dia e mais vendidos".
 * Contrato: docs/issues/nova-dashboard-indicadores.md. A etiqueta é o nome do arquivo.
 * Usa os pedidos da carga de dados (#16), criados nos últimos 60 dias.
 */
@QuarkusTest
@Tag("issue-dashboard-indicadores")
class DashboardIndicadoresResourceTest {

    static JsonPath dashboard(LocalDate inicio, LocalDate fim) {
        return given().queryParam("dataInicio", inicio.toString()).queryParam("dataFim", fim.toString())
                .when().get("/admin/dashboard").then().statusCode(200).extract().jsonPath();
    }

    @Test
    void periodoAnteriorTemOMesmoTamanhoETerminaNaVespera() {
        given().queryParam("dataInicio", "2026-10-01").queryParam("dataFim", "2026-10-10")
                .when().get("/admin/dashboard")
                .then().statusCode(200)
                .body("comparacao.periodoAnterior.dataInicio", equalTo("2026-09-21"))
                .body("comparacao.periodoAnterior.dataFim", equalTo("2026-09-30"));
    }

    @Test
    void vendasPorDiaTemUmPontoPorDiaEmOrdem() {
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusDays(6);
        given().queryParam("dataInicio", inicio.toString()).queryParam("dataFim", fim.toString())
                .when().get("/admin/dashboard")
                .then().statusCode(200)
                .body("vendasPorDia", hasSize(7))
                .body("vendasPorDia[0].data", equalTo(inicio.toString()))
                .body("vendasPorDia[6].data", equalTo(fim.toString()));
    }

    @Test
    void somaDasVendasPorDiaBateComOFaturamento() {
        JsonPath j = dashboard(LocalDate.now().minusDays(90), LocalDate.now());
        BigDecimal soma = j.getList("vendasPorDia.faturamento", String.class).stream()
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal faturamento = new BigDecimal(j.getString("faturamento"));
        assertEquals(0, faturamento.compareTo(soma), "soma de vendasPorDia diferente do faturamento");
    }

    @Test
    void semVendaNoPeriodoAnteriorAVariacaoENula() {
        given().queryParam("dataInicio", "2000-01-01").queryParam("dataFim", "2000-01-31")
                .when().get("/admin/dashboard")
                .then().statusCode(200)
                .body("comparacao.periodoAnterior.dataFim", equalTo("1999-12-31"))
                .body("comparacao.variacaoFaturamento", nullValue())
                .body("comparacao.variacaoPedidos", nullValue());
    }

    @Test
    void maisVendidosAteCincoOrdenadosPorReceita() {
        JsonPath j = dashboard(LocalDate.now().minusDays(90), LocalDate.now());
        assertTrue(j.get("maisVendidos") != null, "resposta sem maisVendidos");
        List<Float> receitas = j.getList("maisVendidos.receita", Float.class);
        assertTrue(receitas.size() <= 5, "mais de 5 itens em maisVendidos");
        for (int i = 1; i < receitas.size(); i++)
            assertTrue(receitas.get(i - 1) >= receitas.get(i), "maisVendidos fora da ordem de receita");
        for (Float p : j.getList("maisVendidos.participacao", Float.class))
            assertTrue(p >= 0 && p <= 100, "participação fora de 0 a 100");
    }

    @Test
    void aguardandoAcaoSoTemPendentePagoOuSeparacaoDoMaisAntigo() {
        JsonPath j = given().when().get("/admin/dashboard").then().statusCode(200)
                .body("pedidosAguardandoAcao.size()", lessThanOrEqualTo(5))
                .body("pedidosAguardandoAcao.status.id", everyItem(oneOf(1, 2, 3)))
                .extract().jsonPath();
        List<String> datas = j.getList("pedidosAguardandoAcao.dataCadastro", String.class);
        for (int i = 1; i < datas.size(); i++)
            assertTrue(datas.get(i - 1).compareTo(datas.get(i)) <= 0, "aguardando ação fora da ordem");
        assertTrue(j.getInt("quantidadeAguardandoAcao") >= datas.size());
    }

    @Test
    void exportaVendasPorDiaEmCsv() {
        byte[] csv = given().queryParam("dataInicio", LocalDate.now().minusDays(6).toString())
                .queryParam("dataFim", LocalDate.now().toString())
                .when().get("/admin/dashboard/exportar")
                .then().statusCode(200).contentType(startsWith("text/csv"))
                .extract().asByteArray();
        assertEquals((byte) 0xEF, csv[0]);
        String[] linhas = new String(csv, StandardCharsets.UTF_8).substring(1).split("\r?\n");
        assertEquals("data;faturamento;pedidos;faturamentoAnterior;pedidosAnterior", linhas[0]);
        assertEquals(8, linhas.length, "cabeçalho + 7 dias");
    }

    @Test
    void exportarComDataFimAntesDoInicioDa400() {
        given().queryParam("dataInicio", "2026-10-10").queryParam("dataFim", "2026-10-01")
                .when().get("/admin/dashboard/exportar")
                .then().statusCode(400)
                .body("errors.field", hasItem("dataFim"));
    }
}

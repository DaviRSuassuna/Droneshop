package br.com.droneshop.testes;

import static io.restassured.RestAssured.given;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Dados de apoio dos testes.
 *
 * Todos os testes rodam no mesmo banco (com a carga do import.sql), então cada
 * teste cria os próprios registros com um sufixo aleatório e nunca depende do
 * total de registros da tabela.
 */
public final class Dados {

    public static final long ID_INEXISTENTE = 999_999_999L;

    /** Código IBGE de Palmas/TO, presente no localidades.sql. */
    public static final int IBGE_PALMAS = 1721000;

    private Dados() {
    }

    /** 8 letras minúsculas: serve em nome, slug e e-mail. */
    public static String sufixo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++)
            sb.append((char) ('a' + ThreadLocalRandom.current().nextInt(26)));
        return sb.toString();
    }

    /** CNPJ válido (com dígitos verificadores), só com dígitos e diferente a cada chamada. */
    public static String cnpjValido() {
        StringBuilder base = new StringBuilder();
        for (int i = 0; i < 8; i++)
            base.append(ThreadLocalRandom.current().nextInt(10));
        base.append("0001");
        int dv1 = digitoCnpj(base.toString(), new int[] { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 });
        int dv2 = digitoCnpj(base.toString() + dv1, new int[] { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 });
        return base.toString() + dv1 + dv2;
    }

    /** 12345678000195 -> 12.345.678/0001-95 */
    public static String mascararCnpj(String digitos) {
        return digitos.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    private static int digitoCnpj(String numeros, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++)
            soma += (numeros.charAt(i) - '0') * pesos[i];
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }

    /** Id do estado pela sigla, consultando a própria API. */
    public static long idEstado(String sigla) {
        return given().when().get("/estados")
                .then().statusCode(200)
                .extract().jsonPath().getLong("find { it.sigla == '" + sigla + "' }.id");
    }

    /** Id do município pelo código IBGE, consultando a própria API. */
    public static long idMunicipio(String siglaEstado, int codigoIbge) {
        return given().queryParam("idEstado", idEstado(siglaEstado))
                .when().get("/municipios")
                .then().statusCode(200)
                .extract().jsonPath().getLong("find { it.codigoIbge == " + codigoIbge + " }.id");
    }
}

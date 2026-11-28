package br.com.droneshop.testes;

import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Ponto de partida de toda requisição dos testes (D25).
 *
 * {@link #given()} entra como o administrador semente da carga de dados
 * (admin@aeria.com.br) e manda o token em toda chamada.
 *
 * Uso: {@code import static br.com.droneshop.testes.Api.given;} no lugar do
 * given() do RestAssured.
 */
public final class Api {

    public static final String ADMIN_EMAIL = "admin@aeria.com.br";
    public static final String ADMIN_SENHA = "aeria123";
    public static final String CLIENTE_EMAIL = "cliente@aeria.com.br";
    public static final String CLIENTE_SENHA = "aeria123";
    public static final String HEADER_CARRINHO = "X-Carrinho-Token";

    private static String tokenAdmin;
    private static long expiraEm;

    private Api() {
    }

    /** Requisição como administrador semente (sem token antes da #4). */
    public static RequestSpecification given() {
        RequestSpecification r = RestAssured.given();
        return r.auth().oauth2(tokenAdmin());
    }

    /** Requisição sem nenhum token (rotas públicas e testes de 401). */
    public static RequestSpecification anonimo() {
        return RestAssured.given();
    }

    /** Requisição com o token informado. */
    public static RequestSpecification como(String token) {
        return RestAssured.given().auth().oauth2(token);
    }

    /** Requisição de carrinho anônimo, com o header do token do carrinho. */
    public static RequestSpecification carrinho(String tokenCarrinho) {
        return RestAssured.given().header(HEADER_CARRINHO, tokenCarrinho);
    }

    /** Requisição de cliente logado com carrinho. */
    public static RequestSpecification carrinho(String tokenCliente, String tokenCarrinho) {
        return como(tokenCliente).header(HEADER_CARRINHO, tokenCarrinho);
    }

    public static synchronized String tokenAdmin() {
        long agora = System.nanoTime();
        if (tokenAdmin == null || agora >= expiraEm) {
            var resposta = tentarLogin(ADMIN_EMAIL, ADMIN_SENHA).then().statusCode(200).extract().jsonPath();
            tokenAdmin = resposta.getString("accessToken");
            expiraEm = agora + java.util.concurrent.TimeUnit.SECONDS.toNanos(resposta.getLong("expiraEm") - 5);
        }
        return tokenAdmin;
    }

    /** Faz login e exige 200; devolve o token. */
    public static String login(String email, String senha) {
        return tentarLogin(email, senha).then().statusCode(200)
                .extract().jsonPath().getString("accessToken");
    }

    public static Response tentarLogin(String email, String senha) {
        return RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("email", email, "senha", senha))
                .when().post("/auth/login");
    }
}

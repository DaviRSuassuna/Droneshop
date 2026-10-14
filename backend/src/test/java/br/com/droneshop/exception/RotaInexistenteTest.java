package br.com.droneshop.exception;

import static br.com.droneshop.testes.Api.given;
import static org.hamcrest.Matchers.endsWith;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/** Rota que não existe também responde no formato Problem (NotFoundExceptionMapper). */
@QuarkusTest
@Tag("base")
class RotaInexistenteTest {

    @Test
    void rotaInexistenteResponde404NoFormatoProblem() {
        given().when().get("/rota-que-nao-existe")
                .then().statusCode(404)
                .contentType("application/problem+json")
                .body("type", endsWith("/errors/not-found"));
    }
}

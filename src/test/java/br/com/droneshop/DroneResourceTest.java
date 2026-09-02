package br.com.droneshop;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class DroneResourceTest {
    @Test
    void deveCriarEListarDrone() {
        String drone = """
                {
                  "nome": "Aeria Pro X1",
                  "sku": "AER-PRO-X1",
                  "preco": 8999.90,
                  "estoque": 5,
                  "autonomiaMin": 42,
                  "alcanceKm": 12.5,
                  "resolucaoVideo": "4K",
                  "pesoGramas": 895,
                  "tipo": "PROFISSIONAL"
                }
                """;

        given()
                .contentType("application/json")
                .body(drone)
                .when().post("/drones")
                .then()
                .statusCode(201)
                .body("nome", is("Aeria Pro X1"));

        given()
                .when().get("/drones")
                .then()
                .statusCode(200)
                .body("nome", hasItem("Aeria Pro X1"));
    }
}
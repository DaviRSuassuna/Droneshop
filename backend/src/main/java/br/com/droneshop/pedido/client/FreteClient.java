package br.com.droneshop.pedido.client;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Agregador de frete (API do Melhor Envio). URL e token em quarkus.rest-client.frete.* e aeria.frete.token;
// nos testes aponta para o FreteFalso (D34).
@RegisterRestClient(configKey = "frete")
@Path("/api/v2/me/shipment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "Authorization", value = "{autorizacao}", required = false)
public interface FreteClient {

    record Cep(@JsonProperty("postal_code") String postalCode) {
    }

    record Pacote(BigDecimal weight, Integer width, Integer height, Integer length) {
    }

    record Cotacao(Cep from, Cep to, @JsonProperty("package") Pacote pacote) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Empresa(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Servico(Long id, String name, BigDecimal price, @JsonProperty("delivery_time") Integer deliveryTime,
            Empresa company, String error) {
    }

    /** Bearer do aeria.frete.token; sem token (dev e testes) o header não vai. */
    default String autorizacao() {
        String t = ConfigProvider.getConfig().getOptionalValue("aeria.frete.token", String.class).orElse("");
        return t.isBlank() ? null : "Bearer " + t;
    }

    @POST
    @Path("/calculate")
    List<Servico> calcular(Cotacao cotacao);
}

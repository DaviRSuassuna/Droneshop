package br.com.droneshop.pagamento.client;

import java.math.BigDecimal;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Mercado Pago em sandbox (D2). URL em quarkus.rest-client.mercadopago.url, token em aeria.mercadopago.token;
// nos testes aponta para o MercadoPagoFalso (D34).
@RegisterRestClient(configKey = "mercadopago")
@Path("/v1/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "Authorization", value = "{autorizacao}", required = false)
public interface MercadoPagoClient {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record NovoPagamento(@JsonProperty("transaction_amount") BigDecimal transactionAmount,
            @JsonProperty("payment_method_id") String paymentMethodId, String token,
            @JsonProperty("external_reference") String externalReference) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PagamentoGateway(String id, String status) {
    }

    record NovoEstorno(BigDecimal amount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EstornoGateway(String id, String status) {
    }

    /** Bearer do aeria.mercadopago.token; sem token (testes) o header não vai. */
    default String autorizacao() {
        String t = ConfigProvider.getConfig().getOptionalValue("aeria.mercadopago.token", String.class).orElse("");
        return t.isBlank() ? null : "Bearer " + t;
    }

    @POST
    PagamentoGateway criar(NovoPagamento pagamento);

    @GET
    @Path("/{id}")
    PagamentoGateway consultar(@PathParam("id") String id);

    @POST
    @Path("/{id}/refunds")
    EstornoGateway estornar(@PathParam("id") String id, NovoEstorno estorno);
}

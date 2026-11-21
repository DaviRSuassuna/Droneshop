package br.com.droneshop.notificacao.client;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Resend (D5). URL em quarkus.rest-client.resend.url, chave em aeria.resend.token;
// nos testes aponta para o ResendFalso (D34).
@RegisterRestClient(configKey = "resend")
@Path("/emails")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "Authorization", value = "{autorizacao}", required = false)
public interface ResendClient {

    record NovoEmail(String from, String to, String subject, String html) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EmailEnviado(String id) {
    }

    /** Bearer do aeria.resend.token; sem chave (testes) o header não vai. */
    default String autorizacao() {
        String t = ConfigProvider.getConfig().getOptionalValue("aeria.resend.token", String.class).orElse("");
        return t.isBlank() ? null : "Bearer " + t;
    }

    @POST
    EmailEnviado enviar(NovoEmail email);
}

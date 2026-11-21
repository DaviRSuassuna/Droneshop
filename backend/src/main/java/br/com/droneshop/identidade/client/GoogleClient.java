package br.com.droneshop.identidade.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Validação do ID token no Google (endpoint tokeninfo, D37). URL em quarkus.rest-client.google.url;
// nos testes aponta para o GoogleFalso (D34).
@RegisterRestClient(configKey = "google")
@Produces(MediaType.APPLICATION_JSON)
public interface GoogleClient {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenInfo(String sub, String email, @JsonProperty("email_verified") String emailVerified, String aud,
            String name) {
    }

    @GET
    @Path("/tokeninfo")
    TokenInfo tokeninfo(@QueryParam("id_token") String idToken);
}

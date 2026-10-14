package br.com.droneshop.identidade;

import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Endpoint tokeninfo do Google falso, que só existe nos testes (D34, D37).
 *
 * No %test, quarkus.rest-client.google.url aponta para cá. O ID token falso
 * tem o formato "sub|email" (e-mail verificado) ou "sub|email|nao-verificado".
 * Qualquer outro formato responde 400, como o Google faz com token inválido.
 */
@Path("/google-falso")
@Produces(MediaType.APPLICATION_JSON)
public class GoogleFalso {

    public static String idToken(String sub, String email) {
        return sub + "|" + email;
    }

    @GET
    @Path("/tokeninfo")
    public Response tokeninfo(@QueryParam("id_token") String idToken) {
        String[] p = idToken == null ? new String[0] : idToken.split("\\|");
        if (p.length < 2 || !p[1].contains("@"))
            return Response.status(400).entity(Map.of("error", "invalid_token")).build();
        boolean verificado = p.length < 3 || !"nao-verificado".equals(p[2]);
        return Response.ok(Map.of("sub", p[0], "email", p[1], "email_verified", String.valueOf(verificado),
                "aud", "aeria-teste")).build();
    }
}

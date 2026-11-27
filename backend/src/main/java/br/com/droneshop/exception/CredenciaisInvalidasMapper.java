package br.com.droneshop.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.com.droneshop.identidade.client.KeycloakClient.CredenciaisInvalidas;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CredenciaisInvalidasMapper implements ExceptionMapper<CredenciaisInvalidas> {

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Context
    UriInfo uri;

    @Override
    public Response toResponse(CredenciaisInvalidas erro) {
        var problem = new Problem();
        problem.type = baseUrl + "/errors/unauthorized";
        problem.title = "Não autenticado";
        problem.status = 401;
        problem.detail = erro.otpNecessario ? "Informe o código OTP." : "Credenciais ausentes ou inválidas.";
        problem.codigo = erro.otpNecessario ? "otp-necessario" : null;
        problem.instance = uri.getRequestUri().getPath();
        problem.timestamp = OffsetDateTime.now();
        return Response.status(401).header("WWW-Authenticate", "Bearer")
                .type("application/problem+json").entity(problem).build();
    }
}

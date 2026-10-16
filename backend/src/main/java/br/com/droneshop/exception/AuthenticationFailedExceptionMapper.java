package br.com.droneshop.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        Problem problem = new Problem();
        problem.type = baseUrl + "/errors/unauthorized";
        problem.title = "Não autenticado";
        problem.status = Response.Status.UNAUTHORIZED.getStatusCode();
        problem.detail = "Credenciais ausentes ou inválidas.";
        problem.instance = uri != null ? uri.getRequestUri().getPath() : null;
        problem.timestamp = OffsetDateTime.now();
        return Response.status(problem.status)
                .header("WWW-Authenticate", "Bearer")
                .type("application/problem+json")
                .entity(problem)
                .build();
    }
}

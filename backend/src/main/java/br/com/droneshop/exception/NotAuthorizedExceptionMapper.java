package br.com.droneshop.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(NotAuthorizedException exception) {
        Problem problem = new Problem();
        problem.type = baseUrl + "/errors/unauthorized";
        problem.title = "Não autenticado";
        problem.status = Response.Status.UNAUTHORIZED.getStatusCode();
        problem.detail = exception.getMessage();
        problem.instance = uri != null ? uri.getRequestUri().getPath() : null;
        problem.timestamp = OffsetDateTime.now();
        return Response.status(problem.status).type("application/problem+json").entity(problem).build();
    }
}

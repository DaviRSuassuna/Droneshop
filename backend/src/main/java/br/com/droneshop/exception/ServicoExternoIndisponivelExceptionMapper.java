package br.com.droneshop.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ServicoExternoIndisponivelExceptionMapper implements ExceptionMapper<ServicoExternoIndisponivelException> {
    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(ServicoExternoIndisponivelException e) {
        var p = new Problem();
        p.type = baseUrl + "/errors/external-service-unavailable";
        p.title = "Serviço externo indisponível";
        p.status = Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
        p.detail = e.getMessage();
        p.instance = (uri != null ? uri.getRequestUri().getPath() : null);
        p.timestamp = OffsetDateTime.now();
        return Response.status(p.status).type("application/problem+json").entity(p).build();
    }
}

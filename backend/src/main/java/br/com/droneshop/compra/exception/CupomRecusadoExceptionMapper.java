package br.com.droneshop.compra.exception;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Mesmo Problem de validação, com o motivo da recusa em cada erro de campo. */
@Provider
@ApplicationScoped
public class CupomRecusadoExceptionMapper implements ExceptionMapper<CupomRecusadoException> {

    public record ErroCupom(String field, String message, String motivo) {
    }

    public record ProblemaCupom(String type, String title, int status, String detail, String instance,
            OffsetDateTime timestamp, List<ErroCupom> errors) {
    }

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(CupomRecusadoException e) {
        var p = new ProblemaCupom(baseUrl + "/errors/validation-error", "Erro de validação", 400, "Dados inválidos",
                uri != null ? uri.getRequestUri().getPath() : null, OffsetDateTime.now(),
                List.of(new ErroCupom("codigo", e.getMessage(), e.getMotivo())));
        return Response.status(400).type("application/problem+json").entity(p).build();
    }
}

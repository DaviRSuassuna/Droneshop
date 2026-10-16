package br.com.droneshop.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class SecurityProblemFilter implements ContainerResponseFilter {

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        int status = response.getStatus();
        String tipo = response.getHeaderString("Content-Type");
        if ((status != 401 && status != 403) || (tipo != null && tipo.startsWith("application/problem+json"))) {
            return;
        }

        Problem problem = new Problem();
        problem.type = baseUrl + (status == 401 ? "/errors/unauthorized" : "/errors/forbidden");
        problem.title = status == 401 ? "Não autenticado" : "Acesso negado";
        problem.status = status;
        problem.detail = status == 401
                ? "Credenciais ausentes ou inválidas."
                : "Usuário sem permissão para este recurso.";
        problem.instance = request.getUriInfo().getRequestUri().getPath();
        problem.timestamp = OffsetDateTime.now();
        response.setEntity(problem);
        response.getHeaders().putSingle("Content-Type", "application/problem+json");
    }
}

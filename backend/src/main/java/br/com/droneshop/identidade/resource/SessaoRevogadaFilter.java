package br.com.droneshop.identidade.resource;

import br.com.droneshop.identidade.service.AutenticacaoService;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Sessão revogada ou expirada não autentica, mesmo com o JWT ainda válido (#20).
 * Token inválido fica para o mecanismo do JWT; aqui só se olha o claim sid.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SessaoRevogadaFilter implements ContainerRequestFilter {

    @Inject
    JWTParser parser;

    @Inject
    AutenticacaoService autenticacao;

    @Override
    public void filter(ContainerRequestContext request) {
        String header = request.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7))
            return;
        Long sid;
        try {
            sid = SessaoAtual.id(parser.parse(header.substring(7).trim()));
        } catch (ParseException e) {
            return;
        }
        if (sid != null && !autenticacao.sessaoValida(sid))
            request.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}

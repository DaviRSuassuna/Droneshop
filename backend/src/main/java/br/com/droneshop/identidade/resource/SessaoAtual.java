package br.com.droneshop.identidade.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.json.JsonNumber;

/** Lê o claim sid (id da SessaoUsuario) do token. */
final class SessaoAtual {

    private SessaoAtual() {
    }

    static Long id(JsonWebToken token) {
        Object sid = token.getClaim("sid");
        if (sid instanceof JsonNumber n)
            return n.longValue();
        if (sid instanceof Number n)
            return n.longValue();
        if (sid instanceof String s && !s.isBlank())
            return Long.valueOf(s);
        return null;
    }
}

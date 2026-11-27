package br.com.droneshop.identidade.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

@RequestScoped
public class UsuarioAtual {

    @Inject
    JsonWebToken token;

    @Inject
    UsuarioRepository usuarios;

    private Usuario usuario;
    private boolean consultado;

    public Usuario usuario() {
        Usuario atual = consultar();
        if (atual == null || !atual.isAtivo())
            throw new ForbiddenException("Usuário inativo ou sem cadastro local.");
        return atual;
    }

    public Long id() {
        return usuario().getId();
    }

    public Long idOuNulo() {
        return token.getSubject() == null ? null : id();
    }

    private Usuario consultar() {
        if (!consultado) {
            usuario = usuarios.porSub(token.getSubject());
            consultado = true;
        }
        return usuario;
    }
}

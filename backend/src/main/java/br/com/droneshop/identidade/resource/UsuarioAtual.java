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
        if (!consultado) {
            usuario = usuarios.porSub(token.getSubject());
            consultado = true;
        }
        if (usuario == null || !usuario.isAtivo())
            throw new ForbiddenException("Usuário inativo ou sem cadastro local.");
        return usuario;
    }

    public Long id() {
        return usuario().getId();
    }

    public Long idOuNulo() {
        return token.getSubject() == null ? null : id();
    }
}

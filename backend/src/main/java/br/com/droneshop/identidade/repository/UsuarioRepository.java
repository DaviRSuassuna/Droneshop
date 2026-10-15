package br.com.droneshop.identidade.repository;

import java.util.Locale;

import br.com.droneshop.identidade.model.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public Usuario porEmail(String email) {
        return find("email", email.trim().toLowerCase(Locale.ROOT)).firstResult();
    }
}

package br.com.droneshop.identidade.repository;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.identidade.model.SessaoUsuario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessaoUsuarioRepository implements PanacheRepository<SessaoUsuario> {

    public List<SessaoUsuario> ativasDo(Long idUsuario) {
        return list("usuario.id = ?1 and revogadaEm is null and expiraEm > ?2 order by id",
                idUsuario, LocalDateTime.now());
    }
}

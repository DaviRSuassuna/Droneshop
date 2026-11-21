package br.com.droneshop.identidade.repository;

import br.com.droneshop.identidade.model.PerfilCliente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PerfilClienteRepository implements PanacheRepository<PerfilCliente> {

    public PerfilCliente porUsuario(Long idUsuario) {
        return find("usuario.id", idUsuario).firstResult();
    }

    public boolean documentoEmUso(String documento) {
        return count("documento", documento) > 0;
    }
}

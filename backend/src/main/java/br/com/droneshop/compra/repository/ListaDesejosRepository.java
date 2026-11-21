package br.com.droneshop.compra.repository;

import br.com.droneshop.compra.model.ListaDesejos;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ListaDesejosRepository implements PanacheRepository<ListaDesejos> {

    public ListaDesejos doUsuario(Long idUsuario) {
        return find("cliente.usuario.id", idUsuario).firstResult();
    }

    public ListaDesejos porToken(String token) {
        return token == null ? null : find("tokenCompartilhamento", token).firstResult();
    }
}

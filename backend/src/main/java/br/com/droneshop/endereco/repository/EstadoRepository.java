package br.com.droneshop.endereco.repository;

import java.util.List;

import br.com.droneshop.endereco.model.Estado;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EstadoRepository implements PanacheRepository<Estado> {

    public List<Estado> findAllOrdenados() {
        return find("SELECT e FROM Estado e ORDER BY e.nome").list();
    }

    public Estado findBySigla(String sigla) {
        return find("SELECT e FROM Estado e WHERE e.sigla = ?1 ", sigla.toUpperCase()).firstResult();
    }

}

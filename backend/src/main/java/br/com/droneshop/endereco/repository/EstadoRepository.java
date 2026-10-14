package br.com.droneshop.endereco.repository;

import br.com.droneshop.endereco.model.Estado;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EstadoRepository implements PanacheRepository<Estado> {

    @Override
    public PanacheQuery<Estado> findAll() {
        return find("SELECT e FROM Estado e ORDER BY e.nome");
    }

    public PanacheQuery<Estado> findByNome(String nome) {
        return find("SELECT e FROM Estado e WHERE LOWER(e.nome) LIKE ?1 ORDER BY e.nome ", "%" + nome.toLowerCase() + "%");
    }

    public Estado findBySigla(String sigla) {
        return find("SELECT e FROM Estado e WHERE e.sigla = ?1 ", sigla.toUpperCase()).firstResult();
    }

}

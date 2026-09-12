package br.com.droneshop.catalogo.repository;

import br.com.droneshop.catalogo.model.Marca;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MarcaRepository implements PanacheRepository<Marca> {

    public Marca findBySlug(String slug) {
        return find("SELECT m FROM Marca m WHERE m.slug = ?1 ", slug).firstResult();
    }

    public Marca findBySlugExceptId(String slug, Long id) {
        if (id == null)
            return findBySlug(slug);
        return find("SELECT m FROM Marca m WHERE m.slug = ?1 AND m.id <> ?2 ", slug, id).firstResult();
    }

    public PanacheQuery<Marca> findByNome(String nome) {
        return find("SELECT m FROM Marca m WHERE m.nome LIKE ?1 ", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Marca> findAll() {
        return find("SELECT m FROM Marca m ORDER BY m.nome");
    }

}

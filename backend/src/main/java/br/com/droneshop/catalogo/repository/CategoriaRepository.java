package br.com.droneshop.catalogo.repository;

import br.com.droneshop.catalogo.model.Categoria;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoriaRepository implements PanacheRepository<Categoria> {
    public Categoria findBySlug(String slug) { return find("slug", slug).firstResult(); }
    public Categoria findBySlugExceptId(String slug, Long id) {
        if (id == null) return findBySlug(slug);
        return find("slug = ?1 and id <> ?2", slug, id).firstResult();
    }
    @Override
    public PanacheQuery<Categoria> findAll() { return find("order by ordemExibicao, nome"); }
}

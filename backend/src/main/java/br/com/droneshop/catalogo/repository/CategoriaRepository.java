package br.com.droneshop.catalogo.repository;

import br.com.droneshop.catalogo.model.Categoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoriaRepository implements PanacheRepository<Categoria> {

    public Categoria findBySlug(String slug) {
        return find("SELECT c FROM Categoria c WHERE c.slug = ?1 ", slug).firstResult();
    }

    public Categoria findBySlugExceptId(String slug, Long id) {
        if (id == null)
            return findBySlug(slug);
        return find("SELECT c FROM Categoria c WHERE c.slug = ?1 AND c.id <> ?2 ", slug, id).firstResult();
    }

    public PanacheQuery<Categoria> findByNome(String nome) {
        return find("SELECT c FROM Categoria c WHERE LOWER(c.nome) LIKE ?1 ORDER BY c.ordemExibicao, c.nome ", "%" + nome.toLowerCase() + "%");
    }



    @Override
    public PanacheQuery<Categoria> findAll() {
        return find("SELECT c FROM Categoria c ORDER BY c.ordemExibicao, c.nome");
    }

}

package br.com.droneshop.catalogo.repository;

import br.com.droneshop.catalogo.model.Subcategoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubcategoriaRepository implements PanacheRepository<Subcategoria> {

    public Subcategoria findBySlugExceptId(String slug, Long id) {
        if (id == null)
            return find("SELECT s FROM Subcategoria s WHERE s.slug = ?1 ", slug).firstResult();
        return find("SELECT s FROM Subcategoria s WHERE s.slug = ?1 AND s.id <> ?2 ", slug, id).firstResult();
    }

    public PanacheQuery<Subcategoria> findByCategoria(Long categoriaId) {
        return find("SELECT s FROM Subcategoria s WHERE s.categoria.id = ?1 ORDER BY s.ordemExibicao, s.nome ", categoriaId);
    }

    public PanacheQuery<Subcategoria> findByNome(String nome) {
        return find("SELECT s FROM Subcategoria s WHERE LOWER(s.nome) LIKE ?1 ORDER BY s.nome ", "%" + nome.toLowerCase() + "%");
    }

    @Override
    public PanacheQuery<Subcategoria> findAll() {
        return find("SELECT s FROM Subcategoria s LEFT JOIN s.categoria c ORDER BY c.ordemExibicao, c.nome, s.ordemExibicao, s.nome");
    }

}

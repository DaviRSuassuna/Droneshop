package br.com.droneshop.catalogo.repository;

import java.util.Locale;
import java.util.HashMap;
import br.com.droneshop.catalogo.model.StatusMarca;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.catalogo.model.Marca;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MarcaRepository implements PanacheRepository<Marca> {
    public PanacheQuery<Marca> findComFiltro(String busca, Long idStatus, String paisOrigem) {
        var params = new HashMap<String, Object>();
        StringBuilder hql = new StringBuilder("1 = 1");
        if (busca != null && !busca.isBlank()) {
            params.put("busca", "%" + busca.trim().toLowerCase(Locale.ROOT) + "%");
            hql.append(" AND (lower(nome) LIKE :busca OR lower(slug) LIKE :busca)");
        }
        if (idStatus != null) {
            var status = StatusMarca.valueOf(idStatus);
            if (status == null) throw ValidationException.of("idStatus", "Status inválido (back).");
            hql.append(" AND status = :status"); params.put("status", status);
        }
        if (paisOrigem != null && !paisOrigem.isBlank()) {
            hql.append(" AND paisOrigem = :extra"); params.put("extra", paisOrigem.trim());
        }
        return find(hql + " ORDER BY nome, id", params);
    }


    public Marca findBySlug(String slug) {
        return find("SELECT m FROM Marca m WHERE m.slug = ?1 ", slug).firstResult();
    }

    public Marca findBySlugExceptId(String slug, Long id) {
        if (id == null)
            return findBySlug(slug);
        return find("SELECT m FROM Marca m WHERE m.slug = ?1 AND m.id <> ?2 ", slug, id).firstResult();
    }

    public PanacheQuery<Marca> findByNome(String nome) {
        return find("SELECT m FROM Marca m WHERE LOWER(m.nome) LIKE ?1 ", "%" + nome.toLowerCase() + "%");
    }

    @Override
    public PanacheQuery<Marca> findAll() {
        return find("SELECT m FROM Marca m ORDER BY m.nome");
    }

}

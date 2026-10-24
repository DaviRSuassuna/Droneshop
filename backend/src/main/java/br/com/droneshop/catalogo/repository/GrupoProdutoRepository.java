package br.com.droneshop.catalogo.repository;

import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.catalogo.model.GrupoProduto;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GrupoProdutoRepository implements PanacheRepository<GrupoProduto> {

    public GrupoProduto findBySlugExceptId(String slug, Long id) {
        if (id == null)
            return find("SELECT g FROM GrupoProduto g WHERE g.slug = ?1 ", slug).firstResult();
        return find("SELECT g FROM GrupoProduto g WHERE g.slug = ?1 AND g.id <> ?2 ", slug, id).firstResult();
    }

    // filtros opcionais: nome vazio e ativo nulo sao ignorados
    public PanacheQuery<GrupoProduto> findComFiltro(String nome, Boolean ativo) {
        StringBuilder jpql = new StringBuilder("SELECT g FROM GrupoProduto g WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();
        if (nome != null && !nome.isBlank()) {
            jpql.append("AND LOWER(g.nome) LIKE :nome ");
            params.put("nome", "%" + nome.trim().toLowerCase() + "%");
        }
        if (ativo != null) {
            jpql.append("AND g.ativo = :ativo ");
            params.put("ativo", ativo);
        }
        jpql.append("ORDER BY g.nome");
        return find(jpql.toString(), params);
    }

}

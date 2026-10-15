package br.com.droneshop.catalogo.repository;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepository<Produto> {

    private static final String COM_ACENTO = "áàâãäéèêëíìîïóòôõöúùûüç";
    private static final String SEM_ACENTO = "aaaaaeeeeiiiiooooouuuuc";

    private static String semAcento(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    public Produto findBySlug(String slug) {
        return find("SELECT p FROM Produto p WHERE p.slug = ?1 ", slug).firstResult();
    }

    public Produto findBySlugExceptId(String slug, Long id) {
        if (id == null)
            return findBySlug(slug);
        return find("SELECT p FROM Produto p WHERE p.slug = ?1 AND p.id <> ?2 ", slug, id).firstResult();
    }

    public Produto findByModeloExceptId(String modelo, Long id) {
        if (id == null)
            return find("SELECT p FROM Produto p WHERE p.modelo = ?1 ", modelo).firstResult();
        return find("SELECT p FROM Produto p WHERE p.modelo = ?1 AND p.id <> ?2 ", modelo, id).firstResult();
    }

    // Filtro combinavel: cada parametro nulo/vazio e simplesmente ignorado.
    // Usado tanto para a listagem paginada (.page) quanto para o total (.count).
    public PanacheQuery<Produto> findComFiltro(String nome, Long marcaId, Long categoriaId, Long subcategoriaId,
                                              StatusProduto status) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Produto p WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        if (nome != null && !nome.isBlank()) {
            // busca sem diferenciar maiusculas nem acentos ("hel" encontra "Hélices")
            jpql.append("AND (CAST(FUNCTION('translate', LOWER(p.nome), :comAcento, :semAcento) AS String) LIKE :nome ")
                .append("OR LOWER(p.modelo) LIKE :nome) ");
            params.put("nome", "%" + semAcento(nome.trim().toLowerCase()) + "%");
            params.put("comAcento", COM_ACENTO);
            params.put("semAcento", SEM_ACENTO);
        }
        if (marcaId != null) {
            jpql.append("AND p.marca.id = :marcaId ");
            params.put("marcaId", marcaId);
        }
        if (categoriaId != null) {
            jpql.append("AND p.subcategoria.categoria.id = :categoriaId ");
            params.put("categoriaId", categoriaId);
        }
        if (subcategoriaId != null) {
            jpql.append("AND p.subcategoria.id = :subcategoriaId ");
            params.put("subcategoriaId", subcategoriaId);
        }
        if (status != null) {
            jpql.append("AND p.status = :status ");
            params.put("status", status);
        }

        jpql.append("ORDER BY p.nome");
        return find(jpql.toString(), params);
    }

}

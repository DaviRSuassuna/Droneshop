package br.com.droneshop.catalogo.repository;

import java.text.Normalizer;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepository<Produto> {

    private static final String COM_ACENTO = "áàâãäéèêëíìîïóòôõöúùûüç";
    private static final String SEM_ACENTO = "aaaaaeeeeiiiiooooouuuuc";

    private static String semAcento(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
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
        Parameters params = new Parameters();

        if (nome != null && !nome.isBlank()) {
            // busca sem diferenciar maiusculas nem acentos ("hel" encontra "Hélices")
            jpql.append("AND (CAST(FUNCTION('translate', LOWER(p.nome), :comAcento, :semAcento) AS String) LIKE :nome ")
                .append("OR LOWER(p.modelo) LIKE :nome) ");
            params.and("nome", "%" + semAcento(nome.trim().toLowerCase()) + "%")
                  .and("comAcento", COM_ACENTO)
                  .and("semAcento", SEM_ACENTO);
        }
        if (marcaId != null) {
            jpql.append("AND p.marca.id = :marcaId ");
            params.and("marcaId", marcaId);
        }
        if (categoriaId != null) {
            jpql.append("AND p.subcategoria.categoria.id = :categoriaId ");
            params.and("categoriaId", categoriaId);
        }
        if (subcategoriaId != null) {
            jpql.append("AND p.subcategoria.id = :subcategoriaId ");
            params.and("subcategoriaId", subcategoriaId);
        }
        if (status != null) {
            jpql.append("AND p.status = :status ");
            params.and("status", status);
        }

        jpql.append("ORDER BY p.nome");
        return find(jpql.toString(), params);
    }

}

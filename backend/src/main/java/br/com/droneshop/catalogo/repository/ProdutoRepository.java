package br.com.droneshop.catalogo.repository;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
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

    // SKU e unico na loja toda (D40)
    public Produto findBySkuExceptId(String sku, Long id) {
        if (id == null)
            return find("SELECT p FROM Produto p WHERE p.sku = ?1 ", sku).firstResult();
        return find("SELECT p FROM Produto p WHERE p.sku = ?1 AND p.id <> ?2 ", sku, id).firstResult();
    }

    // Rotulo e unico dentro do grupo, sem diferenciar maiusculas
    public Produto findByRotuloNoGrupoExceptId(Long idGrupo, String rotulo, Long id) {
        if (id == null)
            return find("SELECT p FROM Produto p WHERE p.grupo.id = ?1 AND LOWER(p.rotuloNoGrupo) = ?2 ",
                    idGrupo, rotulo.toLowerCase()).firstResult();
        return find("SELECT p FROM Produto p WHERE p.grupo.id = ?1 AND LOWER(p.rotuloNoGrupo) = ?2 AND p.id <> ?3 ",
                idGrupo, rotulo.toLowerCase(), id).firstResult();
    }

    public long countByGrupo(Long idGrupo) {
        return count("grupo.id = ?1", idGrupo);
    }

    // Filtro combinavel: cada parametro nulo/vazio e simplesmente ignorado.
    // Usado tanto para a listagem paginada (.page) quanto para o total (.count).
    // idStatus filtra o ciclo de publicação; semKit tira os kits (estoque e
    // componente de kit, que nao aceitam kit).
    public PanacheQuery<Produto> findComFiltro(TipoProduto tipo, String nome, Long marcaId, Long categoriaId,
                                              Long subcategoriaId, StatusProduto status, Long grupoId,
                                              boolean semKit, SituacaoEstoque estoque) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Produto p WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        if (tipo != null)
            jpql.append(switch (tipo) {
                case DRONE -> "AND TYPE(p) = Drone ";
                case ACESSORIO -> "AND TYPE(p) = Acessorio ";
                case KIT -> "AND TYPE(p) = Kit ";
            });
        if (semKit)
            jpql.append("AND TYPE(p) <> Kit ");


        if (nome != null && !nome.isBlank()) {
            // busca sem diferenciar maiusculas nem acentos ("hel" encontra "Hélices")
            jpql.append("AND (CAST(FUNCTION('translate', LOWER(p.nome), :comAcento, :semAcento) AS String) LIKE :nome ")
                .append("OR LOWER(p.sku) LIKE :nome) ");
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
        // mesma regra do GET /estoque?situacao= (#13): kit fica de fora, o saldo e do componente
        if (estoque == SituacaoEstoque.ABAIXO_MINIMO)
            jpql.append("AND TYPE(p) <> Kit AND COALESCE(p.estoqueFisico, 0) < p.estoqueMinimo ");
        else if (estoque == SituacaoEstoque.ZERADO)
            jpql.append("AND TYPE(p) <> Kit AND COALESCE(p.estoqueFisico, 0) <= 0 ");
        if (grupoId != null) {
            jpql.append("AND p.grupo.id = :grupoId ");
            params.put("grupoId", grupoId);
        }
        if (status != null) {
            jpql.append("AND p.status = :status ");
            params.put("status", status);
        }

        jpql.append("ORDER BY p.nome");
        return find(jpql.toString(), params);
    }

}

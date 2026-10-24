package br.com.droneshop.estoque.repository;

import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Visao de estoque: o saldo e de cada produto (RF11, D40)
@ApplicationScoped
public class EstoqueRepository implements PanacheRepository<Produto> {

    // Situacao so olha produto com estoque proprio: o kit nao tem (D7). Ate a #23 o
    // disponivel e o proprio fisico (KitCalculo.disponivel); com as reservas, este
    // filtro passa a descontar o reservado tambem.
    public PanacheQuery<Produto> findComFiltro(SituacaoEstoque situacao, String busca, Long idMarca) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Produto p WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        if (busca != null && !busca.isBlank()) {
            jpql.append("AND (LOWER(p.sku) LIKE :busca OR LOWER(p.nome) LIKE :busca) ");
            params.put("busca", "%" + busca.trim().toLowerCase() + "%");
        }
        if (idMarca != null) {
            jpql.append("AND p.marca.id = :idMarca ");
            params.put("idMarca", idMarca);
        }
        if (situacao == SituacaoEstoque.ABAIXO_MINIMO)
            jpql.append("AND TYPE(p) <> Kit AND COALESCE(p.estoqueFisico, 0) < p.estoqueMinimo ");
        else if (situacao == SituacaoEstoque.ZERADO)
            jpql.append("AND TYPE(p) <> Kit AND COALESCE(p.estoqueFisico, 0) <= 0 ");

        jpql.append("ORDER BY p.nome, p.id");
        return find(jpql.toString(), params);
    }

}

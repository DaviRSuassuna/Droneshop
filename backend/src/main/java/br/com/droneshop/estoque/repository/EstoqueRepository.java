package br.com.droneshop.estoque.repository;

import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Visao de estoque: o saldo e da variante, nunca do produto (RF11)
@ApplicationScoped
public class EstoqueRepository implements PanacheRepository<VarianteProduto> {

    // Situacao so olha variante comum: o kit nao tem estoque proprio (D7). Ate a #23 o
    // disponivel e o proprio fisico (KitCalculo.disponivel); com as reservas, este
    // filtro passa a descontar o reservado tambem.
    public PanacheQuery<VarianteProduto> findComFiltro(SituacaoEstoque situacao, String busca) {
        StringBuilder jpql = new StringBuilder("SELECT v FROM VarianteProduto v WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        if (busca != null && !busca.isBlank()) {
            jpql.append("AND (LOWER(v.sku) LIKE :busca OR LOWER(v.nome) LIKE :busca ")
                .append("OR LOWER(v.produto.nome) LIKE :busca) ");
            params.put("busca", "%" + busca.trim().toLowerCase() + "%");
        }
        if (situacao == SituacaoEstoque.ABAIXO_MINIMO)
            jpql.append("AND v.ehKit = false AND COALESCE(v.estoqueFisico, 0) < v.estoqueMinimo ");
        else if (situacao == SituacaoEstoque.ZERADO)
            jpql.append("AND v.ehKit = false AND COALESCE(v.estoqueFisico, 0) <= 0 ");

        jpql.append("ORDER BY v.produto.nome, v.nome, v.id");
        return find(jpql.toString(), params);
    }

}

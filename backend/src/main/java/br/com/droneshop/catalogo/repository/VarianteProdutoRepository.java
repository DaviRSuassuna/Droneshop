package br.com.droneshop.catalogo.repository;

import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.catalogo.model.VarianteProduto;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VarianteProdutoRepository implements PanacheRepository<VarianteProduto> {

    // SKU e unico na loja toda; idProdutoIgnorado deixa de fora as variantes do proprio
    // produto (o PUT do produto confere a lista inteira de uma vez)
    public VarianteProduto findBySkuForaDoProduto(String sku, Long idProdutoIgnorado) {
        if (idProdutoIgnorado == null)
            return find("SELECT v FROM VarianteProduto v WHERE v.sku = ?1 ", sku).firstResult();
        return find("SELECT v FROM VarianteProduto v WHERE v.sku = ?1 AND v.produto.id <> ?2 ", sku,
                idProdutoIgnorado).firstResult();
    }

    public VarianteProduto findBySkuExceptId(String sku, Long id) {
        return find("SELECT v FROM VarianteProduto v WHERE v.sku = ?1 AND v.id <> ?2 ", sku, id).firstResult();
    }

    // Filtro combinavel: busca procura no SKU, no nome da variante e no nome do produto
    public PanacheQuery<VarianteProduto> findComFiltro(String busca, Boolean ativa) {
        StringBuilder jpql = new StringBuilder("SELECT v FROM VarianteProduto v WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        if (busca != null && !busca.isBlank()) {
            jpql.append("AND (LOWER(v.sku) LIKE :busca OR LOWER(v.nome) LIKE :busca ")
                .append("OR LOWER(v.produto.nome) LIKE :busca) ");
            params.put("busca", "%" + busca.trim().toLowerCase() + "%");
        }
        if (ativa != null) {
            jpql.append("AND v.ativa = :ativa ");
            params.put("ativa", ativa);
        }

        jpql.append("ORDER BY v.produto.nome, v.nome, v.id");
        return find(jpql.toString(), params);
    }

}

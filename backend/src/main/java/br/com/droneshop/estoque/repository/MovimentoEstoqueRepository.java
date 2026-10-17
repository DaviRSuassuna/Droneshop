package br.com.droneshop.estoque.repository;

import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.estoque.model.TipoMovimento;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MovimentoEstoqueRepository implements PanacheRepository<MovimentoEstoque> {

    // mais recente primeiro; o id desempata movimentos gravados no mesmo instante
    public PanacheQuery<MovimentoEstoque> findComFiltro(Long idVariante, TipoMovimento tipo) {
        StringBuilder jpql = new StringBuilder("SELECT m FROM MovimentoEstoque m WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        if (idVariante != null) {
            jpql.append("AND m.variante.id = :idVariante ");
            params.put("idVariante", idVariante);
        }
        if (tipo != null) {
            jpql.append("AND m.tipo = :tipo ");
            params.put("tipo", tipo);
        }

        jpql.append("ORDER BY m.dataCadastro DESC, m.id DESC");
        return find(jpql.toString(), params);
    }

}

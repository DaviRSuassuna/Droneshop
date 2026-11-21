package br.com.droneshop.estoque.repository;

import java.util.List;

import br.com.droneshop.estoque.model.StatusUnidade;
import br.com.droneshop.estoque.model.UnidadeIndividual;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UnidadeIndividualRepository implements PanacheRepository<UnidadeIndividual> {

    public UnidadeIndividual porSerie(String serie) {
        return find("lower(numeroSerie) = ?1", serie.toLowerCase()).firstResult();
    }

    public List<UnidadeIndividual> doItem(Long idItem) {
        return list("itemPedido.id", idItem);
    }

    public PanacheQuery<UnidadeIndividual> filtrar(String serie, Long idProduto, StatusUnidade status) {
        StringBuilder q = new StringBuilder("1 = 1");
        Parameters p = new Parameters();
        if (serie != null && !serie.isBlank()) {
            q.append(" and lower(numeroSerie) like :serie");
            p.and("serie", "%" + serie.trim().toLowerCase() + "%");
        }
        if (idProduto != null) {
            q.append(" and produto.id = :produto");
            p.and("produto", idProduto);
        }
        if (status != null) {
            q.append(" and status = :status");
            p.and("status", status);
        }
        return find(q + " order by id desc", p);
    }

    /** Unidades VENDIDA de pedidos do cliente; mais recente primeiro (#88). */
    public List<UnidadeIndividual> doCliente(Long idUsuario) {
        return list("status = ?1 and itemPedido.pedido.cliente.usuario.id = ?2 order by itemPedido.pedido.dataCadastro desc, id desc",
                StatusUnidade.VENDIDA, idUsuario);
    }

    public UnidadeIndividual doCliente(Long id, Long idUsuario) {
        return find("id = ?1 and status = ?2 and itemPedido.pedido.cliente.usuario.id = ?3", id, StatusUnidade.VENDIDA,
                idUsuario).firstResult();
    }
}

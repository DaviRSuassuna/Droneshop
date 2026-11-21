package br.com.droneshop.devolucao.repository;

import java.util.List;

import br.com.droneshop.devolucao.model.Devolucao;
import br.com.droneshop.devolucao.model.StatusDevolucao;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DevolucaoRepository implements PanacheRepository<Devolucao> {

    public List<Devolucao> doUsuario(Long idUsuario) {
        return list("pedido.cliente.usuario.id = ?1 order by dataSolicitacao desc, id desc", idUsuario);
    }

    public Devolucao doUsuario(Long id, Long idUsuario) {
        return find("id = ?1 and pedido.cliente.usuario.id = ?2", id, idUsuario).firstResult();
    }

    public PanacheQuery<Devolucao> filtrar(StatusDevolucao status) {
        return status == null ? find("order by dataSolicitacao desc, id desc")
                : find("status = ?1 order by dataSolicitacao desc, id desc", status);
    }

    /** Quanto do item já está em devolução não recusada. */
    public int quantidadeDevolvida(Long idItemPedido) {
        Long n = getEntityManager().createQuery("select coalesce(sum(i.quantidade), 0) from ItemDevolucao i "
                + "where i.itemPedido.id = :item and i.devolucao.status <> :recusada", Long.class)
                .setParameter("item", idItemPedido).setParameter("recusada", StatusDevolucao.RECUSADA)
                .getSingleResult();
        return n == null ? 0 : n.intValue();
    }
}

package br.com.droneshop.pedido.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.pedido.dto.PedidoFiltro;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PedidoRepository implements PanacheRepository<Pedido> {

    // busca: nome do cliente (sem diferenciar maiúsculas) ou número do pedido (idBusca)
    public PanacheQuery<Pedido> findComFiltro(PedidoFiltro f) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Pedido p WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        if (f.status() != null) {
            jpql.append("AND p.status = :status ");
            params.put("status", f.status());
        }
        if (f.inicio() != null) {
            jpql.append("AND p.dataCadastro >= :inicio ");
            params.put("inicio", f.inicio());
        }
        if (f.fimExclusivo() != null) {
            jpql.append("AND p.dataCadastro < :fim ");
            params.put("fim", f.fimExclusivo());
        }
        if (f.nomeCliente() != null) {
            jpql.append("AND (LOWER(p.cliente.nome) LIKE :nome ");
            params.put("nome", "%" + f.nomeCliente().toLowerCase() + "%");
            if (f.idBusca() != null) {
                jpql.append("OR p.id = :id ");
                params.put("id", f.idBusca());
            }
            jpql.append(") ");
        }

        jpql.append("ORDER BY p.dataCadastro DESC, p.id DESC");
        return find(jpql.toString(), params);
    }

    public Map<StatusPedido, Long> contarPorStatus() {
        Map<StatusPedido, Long> contagem = new EnumMap<>(StatusPedido.class);
        getEntityManager()
                .createQuery("SELECT p.status, COUNT(p) FROM Pedido p GROUP BY p.status", Object[].class)
                .getResultList()
                .forEach(linha -> contagem.put((StatusPedido) linha[0], (Long) linha[1]));
        return contagem;
    }

    // #18: a mesma contagem, só dos pedidos criados em [inicio, fimExclusivo)
    public Map<StatusPedido, Long> contarPorStatus(LocalDateTime inicio, LocalDateTime fimExclusivo) {
        Map<StatusPedido, Long> contagem = new EnumMap<>(StatusPedido.class);
        getEntityManager()
                .createQuery("SELECT p.status, COUNT(p) FROM Pedido p "
                        + "WHERE p.dataCadastro >= :inicio AND p.dataCadastro < :fim GROUP BY p.status", Object[].class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fimExclusivo)
                .getResultList()
                .forEach(linha -> contagem.put((StatusPedido) linha[0], (Long) linha[1]));
        return contagem;
    }

    public record Totais(long quantidade, BigDecimal valorTotal) {
    }

    // quantidade e soma de valorTotal dos pedidos com um destes status criados em [inicio, fimExclusivo)
    public Totais totalizar(Collection<StatusPedido> status, LocalDateTime inicio, LocalDateTime fimExclusivo) {
        Object[] linha = getEntityManager()
                .createQuery("SELECT COUNT(p), SUM(p.valorTotal) FROM Pedido p WHERE p.status IN :status "
                        + "AND p.dataCadastro >= :inicio AND p.dataCadastro < :fim", Object[].class)
                .setParameter("status", status)
                .setParameter("inicio", inicio)
                .setParameter("fim", fimExclusivo)
                .getSingleResult();
        BigDecimal soma = linha[1] instanceof BigDecimal b ? b : BigDecimal.ZERO;
        return new Totais((Long) linha[0], soma);
    }

    public PanacheQuery<Pedido> doUsuario(Long idUsuario) {
        return find("cliente.usuario.id = ?1 order by dataCadastro desc, id desc", idUsuario);
    }

    public Pedido doUsuario(Long id, Long idUsuario) {
        return find("id = ?1 and cliente.usuario.id = ?2", id, idUsuario).firstResult();
    }
}

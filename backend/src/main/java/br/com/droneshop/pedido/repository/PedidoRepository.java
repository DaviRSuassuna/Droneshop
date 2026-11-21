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
        if (f.metodo() != null) {
            jpql.append("AND EXISTS (SELECT pg FROM Pagamento pg WHERE pg.pedido = p AND pg.vigente = true "
                    + "AND pg.metodo = :metodo) ");
            params.put("metodo", f.metodo());
        }
        if (f.nomeCliente() != null) {
            jpql.append("AND (LOWER(p.cliente.nome) LIKE :nome ");
            params.put("nome", "%" + f.nomeCliente().toLowerCase() + "%");
            if (f.documento() != null) {
                jpql.append("OR p.cliente.documento = :documento ");
                params.put("documento", f.documento());
            }
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

    // #89: soma do valorTotal dos pedidos PENDENTE
    public BigDecimal valorEmAberto() {
        BigDecimal soma = getEntityManager()
                .createQuery("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.status = :status", BigDecimal.class)
                .setParameter("status", StatusPedido.PENDENTE)
                .getSingleResult();
        return soma == null ? BigDecimal.ZERO : soma;
    }

    // #89: devoluções que não terminaram (nem RECUSADA nem CONCLUIDA)
    public long devolucoesAbertas() {
        return getEntityManager()
                .createQuery("SELECT COUNT(d) FROM Devolucao d WHERE d.status NOT IN :fim", Long.class)
                .setParameter("fim", java.util.List.of(br.com.droneshop.devolucao.model.StatusDevolucao.RECUSADA,
                        br.com.droneshop.devolucao.model.StatusDevolucao.CONCLUIDA))
                .getSingleResult();
    }

    // #89: [data ENVIADO, data ENTREGUE] dos pedidos entregues a partir de "desde" (histórico, #17)
    public java.util.List<Object[]> enviosEntregues(LocalDateTime desde) {
        return getEntityManager()
                .createQuery("SELECT MAX(e.dataCadastro), h.dataCadastro FROM HistoricoStatusPedido h, "
                        + "HistoricoStatusPedido e WHERE e.pedido = h.pedido AND h.statusNovo = :entregue "
                        + "AND e.statusNovo = :enviado AND h.dataCadastro >= :desde "
                        + "AND e.dataCadastro <= h.dataCadastro GROUP BY h.id, h.dataCadastro", Object[].class)
                .setParameter("entregue", StatusPedido.ENTREGUE)
                .setParameter("enviado", StatusPedido.ENVIADO)
                .setParameter("desde", desde)
                .getResultList();
    }

    public PanacheQuery<Pedido> doUsuario(Long idUsuario) {
        return find("cliente.usuario.id = ?1 order by dataCadastro desc, id desc", idUsuario);
    }

    public Pedido doUsuario(Long id, Long idUsuario) {
        return find("id = ?1 and cliente.usuario.id = ?2", id, idUsuario).firstResult();
    }
}

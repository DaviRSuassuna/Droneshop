package br.com.droneshop.estoque.repository;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.estoque.model.Reserva;
import br.com.droneshop.estoque.model.StatusReserva;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReservaRepository implements PanacheRepository<Reserva> {

    public int reservadoAtivo(Long idProduto) {
        Long soma = find("select coalesce(sum(r.quantidade), 0) from Reserva r where r.produto.id = ?1 and r.status = ?2",
                idProduto, StatusReserva.ATIVA).project(Long.class).firstResult();
        return soma == null ? 0 : soma.intValue();
    }

    public PanacheQuery<Reserva> ativas(Long idProduto) {
        if (idProduto == null)
            return find("status = ?1 order by id", StatusReserva.ATIVA);
        return find("status = ?1 and produto.id = ?2 order by id", StatusReserva.ATIVA, idProduto);
    }

    public List<Reserva> ativasVencidas(LocalDateTime referencia) {
        return list("status = ?1 and expiraEm < ?2", StatusReserva.ATIVA, referencia);
    }
}

package br.com.droneshop.pedido.repository;

import java.util.List;

import br.com.droneshop.pedido.model.NotaFiscal;
import br.com.droneshop.pedido.model.TipoOrigemNota;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotaFiscalRepository implements PanacheRepository<NotaFiscal> {

    public boolean existe(Long idPedido, TipoOrigemNota tipo) {
        return count("pedido.id = ?1 and tipoOrigem = ?2", idPedido, tipo) > 0;
    }

    public long ultimoNumero(String serie) {
        Long n = getEntityManager()
                .createQuery("select max(cast(n.numero as long)) from NotaFiscal n where n.serie = :serie", Long.class)
                .setParameter("serie", serie).getSingleResult();
        return n == null ? 0 : n;
    }

    public List<NotaFiscal> doPedido(Long idPedido) {
        return list("pedido.id = ?1 order by id", idPedido);
    }
}

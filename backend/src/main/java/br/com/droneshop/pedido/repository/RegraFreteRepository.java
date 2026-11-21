package br.com.droneshop.pedido.repository;

import br.com.droneshop.pedido.model.ModalidadeEntrega;
import br.com.droneshop.pedido.model.RegraFrete;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RegraFreteRepository implements PanacheRepository<RegraFrete> {

    public RegraFrete daModalidade(ModalidadeEntrega m) {
        return find("modalidade = ?1 order by id", m).firstResult();
    }
}

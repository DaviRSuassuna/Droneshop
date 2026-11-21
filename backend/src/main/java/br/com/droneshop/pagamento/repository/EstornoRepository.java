package br.com.droneshop.pagamento.repository;

import br.com.droneshop.pagamento.model.Estorno;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EstornoRepository implements PanacheRepository<Estorno> {
}

package br.com.droneshop.compra.repository;

import br.com.droneshop.compra.model.UsoCupom;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsoCupomRepository implements PanacheRepository<UsoCupom> {
    public long countByCupom(long idCupom) {
        return count("cupom.id", idCupom);
    }

    public long countByCupomECliente(long idCupom, long idCliente) {
        return count("cupom.id = ?1 and cliente.id = ?2", idCupom, idCliente);
    }
}

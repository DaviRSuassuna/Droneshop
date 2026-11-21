package br.com.droneshop.catalogo.repository;

import java.util.List;

import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.StatusProduto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KitRepository implements PanacheRepository<Kit> {

    public List<Kit> findExibidosEmOfertas() {
        return find("status = ?1 and exibirEmOfertas = true order by nome", StatusProduto.ATIVO).list();
    }
}

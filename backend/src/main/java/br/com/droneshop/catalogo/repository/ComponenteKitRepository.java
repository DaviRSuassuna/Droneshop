package br.com.droneshop.catalogo.repository;

import br.com.droneshop.catalogo.model.ComponenteKit;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ComponenteKitRepository implements PanacheRepository<ComponenteKit> {

    // Primeiro kit que usa o produto como componente (impede excluir, D24)
    public ComponenteKit findUsoDoProduto(Long idProduto) {
        return find("SELECT c FROM ComponenteKit c WHERE c.produto.id = ?1 ORDER BY c.id", idProduto).firstResult();
    }

}

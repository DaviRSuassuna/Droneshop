package br.com.droneshop.catalogo.repository;

import java.util.Collection;

import br.com.droneshop.catalogo.model.ComponenteKit;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ComponenteKitRepository implements PanacheRepository<ComponenteKit> {

    // Kit de outro produto que usa alguma destas variantes (impede excluir, D24)
    public ComponenteKit findUsoForaDoProduto(Collection<Long> idsVariantes, Long idProduto) {
        if (idsVariantes.isEmpty())
            return null;
        return find("SELECT c FROM ComponenteKit c WHERE c.componente.id IN ?1 AND c.kit.produto.id <> ?2 ORDER BY c.id",
                idsVariantes, idProduto).firstResult();
    }

}

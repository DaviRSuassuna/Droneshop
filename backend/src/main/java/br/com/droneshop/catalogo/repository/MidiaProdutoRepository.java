package br.com.droneshop.catalogo.repository;

import java.util.List;

import br.com.droneshop.catalogo.model.MidiaProduto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MidiaProdutoRepository implements PanacheRepository<MidiaProduto> {

    public List<MidiaProduto> findByProdutoId(long idProduto) {
        return list("produto.id = ?1 order by ordem, id", idProduto);
    }

    public MidiaProduto findByUrl(String url) {
        return find("url", url).firstResult();
    }

    public List<MidiaProduto> findPrincipais(long idProduto, Long idIgnorado) {
        if (idIgnorado == null)
            return list("produto.id = ?1 and principal = true", idProduto);
        return list("produto.id = ?1 and principal = true and id <> ?2", idProduto, idIgnorado);
    }
}

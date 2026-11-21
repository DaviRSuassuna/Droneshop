package br.com.droneshop.catalogo.repository;

import java.util.List;

import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.StatusProduto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AcessorioRepository implements PanacheRepository<Acessorio> {

    public List<Acessorio> findSugeridosPara(long idProduto) {
        return find("select distinct a from Acessorio a join a.compativeis p "
                + "where p.id = ?1 and a.status = ?2 and a.sugerirNosCompativeis = true order by a.nome",
                idProduto, StatusProduto.ATIVO).list();
    }
}

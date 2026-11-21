package br.com.droneshop.identidade.repository;

import java.util.List;

import br.com.droneshop.identidade.model.ContaSocial;
import br.com.droneshop.identidade.model.ProvedorSocial;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContaSocialRepository implements PanacheRepository<ContaSocial> {

    public ContaSocial porSub(ProvedorSocial provedor, String sub) {
        return find("provedor = ?1 and identificadorExterno = ?2", provedor, sub).firstResult();
    }

    public List<ContaSocial> doUsuario(Long idUsuario) {
        return list("usuario.id = ?1 order by id", idUsuario);
    }
}

package br.com.droneshop.identidade.repository;

import java.util.List;

import br.com.droneshop.identidade.model.ConsentimentoLGPD;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConsentimentoRepository implements PanacheRepository<ConsentimentoLGPD> {

    public List<ConsentimentoLGPD> doUsuario(Long idUsuario) {
        return list("usuario.id = ?1 order by dataConsentimento, id", idUsuario);
    }
}

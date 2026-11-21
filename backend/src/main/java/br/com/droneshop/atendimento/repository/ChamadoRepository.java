package br.com.droneshop.atendimento.repository;

import java.util.List;

import br.com.droneshop.atendimento.model.Chamado;
import br.com.droneshop.atendimento.model.StatusChamado;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChamadoRepository implements PanacheRepository<Chamado> {

    public List<Chamado> doUsuario(Long idUsuario) {
        return list("cliente.usuario.id", Sort.descending("id"), idUsuario);
    }

    public PanacheQuery<Chamado> filtrar(StatusChamado status) {
        if (status == null)
            return findAll(Sort.descending("id"));
        return find("status", Sort.descending("id"), status);
    }

    public long doDia(String prefixo) {
        return count("protocolo like ?1", prefixo + "%");
    }
}

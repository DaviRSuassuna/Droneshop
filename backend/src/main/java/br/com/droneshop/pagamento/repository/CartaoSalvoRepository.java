package br.com.droneshop.pagamento.repository;

import java.util.List;

import br.com.droneshop.pagamento.model.CartaoSalvo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CartaoSalvoRepository implements PanacheRepository<CartaoSalvo> {

    public List<CartaoSalvo> doUsuario(Long idUsuario) {
        return list("cliente.usuario.id = ?1 and ativo = true order by id", idUsuario);
    }

    public CartaoSalvo doUsuario(Long id, Long idUsuario) {
        return find("id = ?1 and cliente.usuario.id = ?2 and ativo = true", id, idUsuario).firstResult();
    }
}

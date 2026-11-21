package br.com.droneshop.compra.repository;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.compra.model.Carrinho;
import br.com.droneshop.compra.model.StatusCarrinho;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CarrinhoRepository implements PanacheRepository<Carrinho> {

    public Carrinho porToken(String token) {
        return token == null ? null : find("tokenSessao", token).firstResult();
    }

    public Carrinho ativoDoUsuario(Long idUsuario) {
        return find("cliente.usuario.id = ?1 and status = ?2 order by id desc", idUsuario, StatusCarrinho.ATIVO)
                .firstResult();
    }

    public List<Carrinho> ativosParadosDesde(LocalDateTime limite) {
        return list("status = ?1 and ultimaAlteracao < ?2", StatusCarrinho.ATIVO, limite);
    }
}

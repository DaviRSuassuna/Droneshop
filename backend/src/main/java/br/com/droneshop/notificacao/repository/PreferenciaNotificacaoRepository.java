package br.com.droneshop.notificacao.repository;

import java.util.List;
import java.util.Optional;

import br.com.droneshop.notificacao.model.PreferenciaNotificacao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PreferenciaNotificacaoRepository implements PanacheRepository<PreferenciaNotificacao> {

    public List<PreferenciaNotificacao> doUsuario(Long idUsuario) {
        return list("usuario.id", idUsuario);
    }

    public Optional<PreferenciaNotificacao> doUsuario(Long idUsuario, String tipo) {
        return find("usuario.id = ?1 and tipoNotificacao = ?2", idUsuario, tipo).firstResultOptional();
    }

    public Optional<PreferenciaNotificacao> porToken(String token) {
        return find("tokenDescadastro", token).firstResultOptional();
    }
}

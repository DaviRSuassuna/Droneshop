package br.com.droneshop.notificacao.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.com.droneshop.notificacao.model.Notificacao;
import br.com.droneshop.notificacao.model.StatusEnvioNotificacao;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificacaoRepository implements PanacheRepository<Notificacao> {

    /** Filtros vazios são ignorados; mais recente primeiro. */
    public PanacheQuery<Notificacao> filtrar(String destinatario, StatusEnvioNotificacao status) {
        StringBuilder q = new StringBuilder("1 = 1");
        Map<String, Object> p = new HashMap<>();
        if (destinatario != null && !destinatario.isBlank()) {
            q.append(" and lower(emailDestino) like :dest");
            p.put("dest", "%" + destinatario.trim().toLowerCase() + "%");
        }
        if (status != null) {
            q.append(" and statusEnvio = :status");
            p.put("status", status);
        }
        return find(q.toString(), Sort.descending("id"), p);
    }

    public Optional<Notificacao> porIdentificador(String identificador) {
        return find("identificadorProvedor", identificador).firstResultOptional();
    }
}

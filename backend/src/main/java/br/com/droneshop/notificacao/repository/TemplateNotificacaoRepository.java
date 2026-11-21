package br.com.droneshop.notificacao.repository;

import java.util.List;
import java.util.Optional;

import br.com.droneshop.notificacao.model.TemplateNotificacao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TemplateNotificacaoRepository implements PanacheRepository<TemplateNotificacao> {

    public List<TemplateNotificacao> ativos() {
        return list("ativo = true order by codigo");
    }

    public Optional<TemplateNotificacao> ativo(String codigo) {
        return find("codigo = ?1 and ativo = true", codigo).firstResultOptional();
    }
}

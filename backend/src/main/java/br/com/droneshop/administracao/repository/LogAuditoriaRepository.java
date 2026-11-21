package br.com.droneshop.administracao.repository;

import br.com.droneshop.administracao.model.LogAuditoria;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogAuditoriaRepository implements PanacheRepository<LogAuditoria> {
}

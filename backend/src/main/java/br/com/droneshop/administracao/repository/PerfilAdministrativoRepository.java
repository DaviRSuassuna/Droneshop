package br.com.droneshop.administracao.repository;

import br.com.droneshop.administracao.model.PerfilAdministrativo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PerfilAdministrativoRepository implements PanacheRepository<PerfilAdministrativo> {
}

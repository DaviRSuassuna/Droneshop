package br.com.droneshop.atendimento.repository;

import br.com.droneshop.atendimento.model.LeadEmpresa;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LeadEmpresaRepository implements PanacheRepository<LeadEmpresa> {
}

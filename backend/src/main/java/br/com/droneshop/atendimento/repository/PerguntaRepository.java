package br.com.droneshop.atendimento.repository;

import br.com.droneshop.atendimento.model.Pergunta;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PerguntaRepository implements PanacheRepository<Pergunta> {
}

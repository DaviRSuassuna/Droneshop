package br.com.droneshop.catalogo.repository;

import java.time.LocalDateTime;

import br.com.droneshop.catalogo.model.Promocao;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PromocaoRepository implements PanacheRepository<Promocao> {

    public PanacheQuery<Promocao> findBySituacao(String situacao, LocalDateTime agora) {
        return switch (situacao) {
            case "VIGENTE" -> find("ativa = true and dataInicio <= ?1 and dataFim >= ?1 order by dataInicio desc", agora);
            case "AGENDADA" -> find("ativa = true and dataInicio > ?1 order by dataInicio", agora);
            case "ENCERRADA" -> find("ativa = true and dataFim < ?1 order by dataFim desc", agora);
            case "INATIVA" -> find("ativa = false order by dataAlteracao desc, id desc");
            case "" -> findAll();
            default -> throw new IllegalArgumentException("Situação de promoção inválida: " + situacao);
        };
    }
}

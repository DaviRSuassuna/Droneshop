package br.com.droneshop.compra.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.compra.model.Cupom;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CupomRepository implements PanacheRepository<Cupom> {

    public Cupom findByCodigo(String codigo) {
        return find("upper(codigo) = ?1", codigo.toUpperCase()).firstResult();
    }

    public PanacheQuery<Cupom> findComFiltro(String situacao, String codigo, LocalDateTime agora) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Cupom c WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();
        if (codigo != null && !codigo.isBlank()) {
            jpql.append("AND LOWER(c.codigo) LIKE :codigo ");
            params.put("codigo", "%" + codigo.trim().toLowerCase() + "%");
        }
        if (situacao != null && !situacao.isBlank()) {
            switch (situacao) {
                case "VIGENTE" -> {
                    jpql.append("AND c.ativo = true AND c.dataInicio <= :agora AND (c.dataFim IS NULL OR c.dataFim >= :agora) ");
                    jpql.append("AND (c.limiteTotalUso IS NULL OR SIZE(c.usos) < c.limiteTotalUso) ");
                }
                case "AGENDADO" -> jpql.append("AND c.ativo = true AND c.dataInicio > :agora ");
                case "ENCERRADO" -> jpql.append("AND c.ativo = true AND c.dataFim < :agora ");
                case "ESGOTADO" -> jpql.append("AND c.ativo = true AND c.limiteTotalUso IS NOT NULL AND SIZE(c.usos) >= c.limiteTotalUso ");
                case "INATIVO", "PAUSADO" -> jpql.append("AND c.ativo = false ");
                default -> throw new IllegalArgumentException("Situação inválida.");
            }
            params.put("agora", agora);
        }
        jpql.append("ORDER BY c.dataCadastro DESC, c.id DESC");
        return find(jpql.toString(), params);
    }
}

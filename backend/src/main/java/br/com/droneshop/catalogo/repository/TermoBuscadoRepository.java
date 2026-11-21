package br.com.droneshop.catalogo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.catalogo.model.TermoBuscado;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TermoBuscadoRepository implements PanacheRepository<TermoBuscado> {

    public void registrar(String termo, int resultados) {
        TermoBuscado t = find("termo", termo).firstResult();
        boolean novo = t == null;
        if (novo) {
            t = new TermoBuscado();
            t.setTermo(termo);
        }
        t.setBuscas(t.getBuscas() + 1);
        t.setUltimaQuantidadeResultados(resultados);
        t.setUltimaBusca(LocalDateTime.now());
        // id IDENTITY: o insert sai no persist, então os campos já precisam estar preenchidos
        if (novo)
            persist(t);
    }

    /** Filtros vazios são ignorados; mais buscados primeiro. */
    public PanacheQuery<TermoBuscado> relatorio(Boolean semResultado, LocalDate inicio, LocalDate fim) {
        StringBuilder q = new StringBuilder("1 = 1");
        Map<String, Object> p = new HashMap<>();
        if (Boolean.TRUE.equals(semResultado))
            q.append(" and ultimaQuantidadeResultados = 0");
        if (inicio != null) {
            q.append(" and ultimaBusca >= :inicio");
            p.put("inicio", inicio.atStartOfDay());
        }
        if (fim != null) {
            q.append(" and ultimaBusca < :fim");
            p.put("fim", fim.plusDays(1).atStartOfDay());
        }
        return find(q.toString(), Sort.descending("buscas").and("termo"), p);
    }
}

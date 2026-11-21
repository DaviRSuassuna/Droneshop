package br.com.droneshop.catalogo.resource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.catalogo.model.TermoBuscado;
import br.com.droneshop.catalogo.repository.TermoBuscadoRepository;
import br.com.droneshop.dto.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Relatório de termos buscados, agrupado por termo (#43). */
@Path("/admin/termos-buscados")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.CATALOGO)
public class TermoBuscadoResource {

    public record TermoBuscadoResponseDTO(String termo, int buscas, int ultimaQuantidadeResultados,
            LocalDateTime ultimaBusca) {
        static TermoBuscadoResponseDTO from(TermoBuscado t) {
            return new TermoBuscadoResponseDTO(t.getTermo(), t.getBuscas(), t.getUltimaQuantidadeResultados(),
                    t.getUltimaBusca());
        }
    }

    @Inject
    TermoBuscadoRepository repository;

    @GET
    @Transactional
    public PageResponse<TermoBuscadoResponseDTO> listar(@QueryParam("semResultado") Boolean semResultado,
            @QueryParam("dataInicio") LocalDate dataInicio, @QueryParam("dataFim") LocalDate dataFim,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        var q = repository.relatorio(semResultado, dataInicio, dataFim);
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(),
                TermoBuscadoResponseDTO::from);
    }
}

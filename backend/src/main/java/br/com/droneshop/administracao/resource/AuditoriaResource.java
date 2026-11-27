package br.com.droneshop.administracao.resource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.AcaoAuditoria;
import br.com.droneshop.administracao.model.LogAuditoria;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.administracao.model.PapelAdministrativo;
import br.com.droneshop.administracao.repository.LogAuditoriaRepository;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.identidade.model.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Consulta do log de auditoria, só para o papel ADMIN (#41, RF45). */
@Path("/admin/auditoria")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ADMINISTRACAO)
public class AuditoriaResource {

    public record Autor(Long id, String nome) {
    }

    public record LogAuditoriaResponseDTO(AcaoAuditoria acao, String entidade, String entidadeId, String campo,
            String valorAnterior, String valorNovo, Autor autor, String ip, LocalDateTime dataHora) {

        static LogAuditoriaResponseDTO from(LogAuditoria l) {
            Usuario u = l.getAutor();
            return new LogAuditoriaResponseDTO(l.getAcao(), l.getEntidade(), l.getEntidadeId(), l.getCampo(),
                    l.getValorAnterior(), l.getValorNovo(), u == null ? null : new Autor(u.getId(), u.getNome()),
                    l.getIp(), l.getDataCadastro());
        }
    }

    @Inject
    LogAuditoriaRepository repository;

    @Inject
    EntityManager em;

    @Inject
    UsuarioAtual usuarioAtual;

    @GET
    @Transactional
    public PageResponse<LogAuditoriaResponseDTO> listar(@QueryParam("entidade") String entidade,
            @QueryParam("entidadeId") String entidadeId, @QueryParam("idAcao") Long idAcao,
            @QueryParam("idUsuario") Long idUsuario,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        Usuario eu = em.find(Usuario.class, usuarioAtual.id());
        if (eu == null || eu.getPerfilAdministrativo() == null
                || eu.getPerfilAdministrativo().getPapel() != PapelAdministrativo.ADMIN)
            throw new ForbiddenException("Só o papel ADMIN consulta a auditoria.");

        StringBuilder q = new StringBuilder("1 = 1");
        Map<String, Object> p = new HashMap<>();
        if (entidade != null && !entidade.isBlank()) {
            q.append(" and entidade = :entidade");
            p.put("entidade", entidade.trim());
        }
        if (entidadeId != null && !entidadeId.isBlank()) {
            q.append(" and entidadeId = :entidadeId");
            p.put("entidadeId", entidadeId.trim());
        }
        AcaoAuditoria acao = AcaoAuditoria.valueOf(idAcao);
        if (acao != null) {
            q.append(" and acao = :acao");
            p.put("acao", acao);
        }
        if (idUsuario != null) {
            q.append(" and autor.id = :autor");
            p.put("autor", idUsuario);
        }
        PanacheQuery<LogAuditoria> consulta = repository.find(q.toString(), Sort.descending("id"), p);
        return PageResponse.of(consulta.page(page, pageSize).list(), page, pageSize, consulta.count(),
                LogAuditoriaResponseDTO::from);
    }
}

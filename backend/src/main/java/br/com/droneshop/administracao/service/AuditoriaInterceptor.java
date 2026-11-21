package br.com.droneshop.administracao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

import br.com.droneshop.administracao.model.AcaoAuditoria;
import br.com.droneshop.administracao.model.LogAuditoria;
import br.com.droneshop.catalogo.model.TermoBuscado;
import br.com.droneshop.identidade.model.SessaoUsuario;
import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.notificacao.model.Notificacao;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Entity;

/**
 * Log de auditoria das ações administrativas (#41, RF45, RNF11). Registra criação, atualização,
 * exclusão e mudança de status feitas por quem está com o perfil administrativo, e o login no
 * admin. Uma linha por campo; dado pessoal vira *** (RNF03). Grava na mesma conexão da
 * transação: a auditoria só existe se a mudança for confirmada.
 */
@PersistenceUnitExtension
@ApplicationScoped
public class AuditoriaInterceptor implements Interceptor {

    private static final Logger LOG = Logger.getLogger(AuditoriaInterceptor.class);
    private static final String MASCARA = "***";
    private static final Set<String> PESSOAIS = Set.of("documento", "cpf", "cnpj", "telefone", "celular", "email",
            "senha", "senhahash", "endereco", "logradouro", "numero", "complemento", "bairro", "cep", "token",
            "tokenhash", "segredo", "segredodoisfatores", "nascimento", "datanascimento");
    private static final Set<String> IGNORADOS = Set.of("dataCadastro", "dataAlteracao");
    private static final Set<Class<?>> FORA = Set.of(LogAuditoria.class, TermoBuscado.class, Notificacao.class);

    private record Contexto(Long autor, String ip) {
    }

    @Override
    public boolean onFlushDirty(Object entity, Object id, Object[] atual, Object[] anterior, String[] nomes,
            Type[] tipos) {
        Contexto ctx = administrativo(entity);
        if (ctx == null)
            return false;
        for (int i = 0; i < nomes.length; i++) {
            if (IGNORADOS.contains(nomes[i]) || atual[i] instanceof Collection<?> || atual[i] instanceof Map<?, ?>)
                continue;
            Object antes = anterior == null ? null : anterior[i];
            String a = texto(nomes[i], antes);
            String n = texto(nomes[i], atual[i]);
            if (anterior != null && Objects.equals(a, n))
                continue;
            AcaoAuditoria acao = "status".equals(nomes[i]) ? AcaoAuditoria.MUDANCA_STATUS : AcaoAuditoria.ATUALIZACAO;
            gravar(acao, entity, id, nomes[i], a, n, ctx);
        }
        return false;
    }

    @Override
    public boolean onPersist(Object entity, Object id, Object[] state, String[] nomes, Type[] tipos) {
        if (entity instanceof SessaoUsuario s && s.getPerfilAtivo() != null
                && "ADMINISTRATIVO".equals(s.getPerfilAtivo().getNome()) && s.getUsuario() != null) {
            gravar(AcaoAuditoria.LOGIN, s.getUsuario(), s.getUsuario().getId(), null, null, null,
                    new Contexto(s.getUsuario().getId(), ip()));
            return false;
        }
        Contexto ctx = administrativo(entity);
        if (ctx != null)
            gravar(AcaoAuditoria.CRIACAO, entity, id, null, null, null, ctx);
        return false;
    }

    @Override
    public void onRemove(Object entity, Object id, Object[] state, String[] nomes, Type[] tipos) {
        Contexto ctx = administrativo(entity);
        if (ctx != null)
            gravar(AcaoAuditoria.EXCLUSAO, entity, id, null, null, null, ctx);
    }

    /** Contexto do autor quando a requisição é de um administrativo; null nos demais casos. */
    private static Contexto administrativo(Object entity) {
        if (FORA.contains(entity.getClass()) || !(entity instanceof DefaultEntity))
            return null;
        if (!Arc.container().requestContext().isActive())
            return null;
        try (InstanceHandle<SecurityIdentity> h = Arc.container().instance(SecurityIdentity.class)) {
            SecurityIdentity identidade = h.isAvailable() ? h.get() : null;
            if (identidade == null || identidade.isAnonymous() || !identidade.hasRole("ADMINISTRATIVO"))
                return null;
            Long autor = null;
            try {
                autor = Long.valueOf(identidade.getPrincipal().getName());
            } catch (RuntimeException e) {
                // sem id numérico no token, a linha fica sem autor
            }
            return new Contexto(autor, ip());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String ip() {
        try (InstanceHandle<CurrentVertxRequest> h = Arc.container().instance(CurrentVertxRequest.class)) {
            RoutingContext rc = h.isAvailable() ? h.get().getCurrent() : null;
            if (rc == null)
                return null;
            String encaminhado = rc.request().getHeader("X-Forwarded-For");
            if (encaminhado != null && !encaminhado.isBlank())
                return encaminhado.split(",")[0].trim();
            return rc.request().remoteAddress() == null ? null : rc.request().remoteAddress().host();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String texto(String campo, Object valor) {
        if (valor == null)
            return null;
        if (PESSOAIS.contains(campo.toLowerCase(Locale.ROOT)))
            return MASCARA;
        if (valor instanceof BigDecimal b)
            return b.scale() < 2 ? b.setScale(2, RoundingMode.HALF_UP).toPlainString() : b.toPlainString();
        if (valor instanceof Enum<?> e) {
            try {
                return String.valueOf(e.getClass().getMethod("getNome").invoke(e));
            } catch (ReflectiveOperationException ex) {
                return e.name();
            }
        }
        if (valor instanceof DefaultEntity d)
            return d.getId() == null ? null : String.valueOf(d.getId());
        return valor.toString();
    }

    /** Nome da entidade raiz da hierarquia: Drone, Acessorio e Kit aparecem como Produto. */
    static String entidade(Object entity) {
        Class<?> c = entity.getClass();
        while (c.getSuperclass() != null && c.getSuperclass().isAnnotationPresent(Entity.class))
            c = c.getSuperclass();
        String nome = c.getSimpleName();
        int proxy = nome.indexOf('$');
        return proxy > 0 ? nome.substring(0, proxy) : nome;
    }

    private static void gravar(AcaoAuditoria acao, Object entity, Object id, String campo, String anterior,
            String novo, Contexto ctx) {
        DataSource ds = Arc.container().instance(DataSource.class).get();
        String sql = "insert into log_auditoria (acao, entidade, entidade_id, campo, valor_anterior, valor_novo,"
                + " id_autor, ip, dataCadastro) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // a conexão é a mesma da transação JTA em curso (Agroal); fechar só devolve o handle
        try (Connection con = ds.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, acao.getId());
            ps.setString(2, entidade(entity));
            ps.setString(3, id == null ? null : String.valueOf(id));
            ps.setString(4, campo);
            ps.setString(5, anterior);
            ps.setString(6, novo);
            if (ctx.autor() == null)
                ps.setNull(7, Types.BIGINT);
            else
                ps.setLong(7, ctx.autor());
            ps.setString(8, ctx.ip());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.warnf("Auditoria não gravada (%s %s): %s", acao.getNome(), entidade(entity), e.getMessage());
        }
    }
}

package br.com.droneshop.notificacao.service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.notificacao.client.ResendClient;
import br.com.droneshop.notificacao.dto.NotificacaoResponseDTO;
import br.com.droneshop.notificacao.dto.TemplateEmailRequestDTO;
import br.com.droneshop.notificacao.dto.TemplateEmailResponseDTO;
import br.com.droneshop.notificacao.dto.WebhookResendDTO;
import br.com.droneshop.notificacao.model.Notificacao;
import br.com.droneshop.notificacao.model.StatusEnvioNotificacao;
import br.com.droneshop.notificacao.model.TemplateNotificacao;
import br.com.droneshop.notificacao.repository.NotificacaoRepository;
import br.com.droneshop.notificacao.repository.TemplateNotificacaoRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class NotificacaoServiceImpl implements NotificacaoService {

    private static final Logger LOG = Logger.getLogger(NotificacaoServiceImpl.class);
    private static final Pattern VARIAVEL = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

    @Inject
    TemplateNotificacaoRepository templates;

    @Inject
    NotificacaoRepository notificacoes;

    @Inject
    EntityManager em;

    @Inject
    @RestClient
    ResendClient resend;

    @ConfigProperty(name = "aeria.resend.remetente", defaultValue = "Aeria Drones <onboarding@resend.dev>")
    String remetente;

    @Override
    public List<TemplateEmailResponseDTO> templates() {
        return templates.ativos().stream().map(TemplateEmailResponseDTO::from).toList();
    }

    @Override
    public TemplateEmailResponseDTO template(String codigo) {
        return TemplateEmailResponseDTO.from(ativo(codigo));
    }

    private TemplateNotificacao ativo(String codigo) {
        return templates.ativo(codigo).orElseThrow(() -> new ResourceNotFoundException("Template não encontrado."));
    }

    @Override
    @Transactional
    public TemplateEmailResponseDTO editar(String codigo, TemplateEmailRequestDTO dto) {
        TemplateNotificacao atual = ativo(codigo);
        List<String> oferecidas = atual.listaVariaveis();
        Matcher m = VARIAVEL.matcher(dto.corpoTemplate() + " " + dto.assunto());
        while (m.find())
            if (!oferecidas.contains(m.group(1)))
                throw ValidationException.of("corpoTemplate",
                        "O template não oferece a variável " + m.group(1) + " (back)");
        atual.setAtivo(false);
        TemplateNotificacao nova = new TemplateNotificacao();
        nova.setCodigo(atual.getCodigo());
        nova.setCategoria(atual.getCategoria());
        nova.setVariaveis(atual.getVariaveis());
        nova.setAssunto(dto.assunto().trim());
        nova.setCorpoTemplate(dto.corpoTemplate());
        nova.setVersao(atual.getVersao() + 1);
        nova.setAtivo(true);
        templates.persist(nova);
        return TemplateEmailResponseDTO.from(nova);
    }

    @Override
    @Transactional
    public PageResponse<NotificacaoResponseDTO> listar(String destinatario, Long idStatusEnvio, int page,
            int pageSize) {
        var q = notificacoes.filtrar(destinatario, StatusEnvioNotificacao.valueOf(idStatusEnvio));
        List<Notificacao> itens = q.page(page, pageSize).list();
        return PageResponse.of(itens, page, pageSize, q.count(), NotificacaoResponseDTO::from);
    }

    @Override
    @Transactional
    public void webhook(WebhookResendDTO evento) {
        if (evento == null || evento.data() == null || evento.data().emailId() == null || evento.type() == null)
            return;
        StatusEnvioNotificacao novo = switch (evento.type()) {
            case "email.delivered" -> StatusEnvioNotificacao.ENTREGUE;
            case "email.opened" -> StatusEnvioNotificacao.ABERTA;
            case "email.bounced" -> StatusEnvioNotificacao.BOUNCE;
            case "email.complained" -> StatusEnvioNotificacao.SPAM;
            default -> null;
        };
        if (novo != null)
            notificacoes.porIdentificador(evento.data().emailId()).ifPresent(n -> n.setStatusEnvio(novo));
    }

    static String renderizar(String texto, Map<String, String> variaveis) {
        Matcher m = VARIAVEL.matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String v = variaveis.getOrDefault(m.group(1), "");
            m.appendReplacement(sb, Matcher.quoteReplacement(v == null ? "" : v));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public void enviar(String codigo, String email, Long idUsuario, Map<String, String> variaveis) {
        Long idNotificacao;
        String assunto;
        String html;
        try {
            Object[] r = QuarkusTransaction.requiringNew().call(() -> {
                TemplateNotificacao t = ativo(codigo);
                Notificacao n = new Notificacao();
                n.setTemplate(t);
                n.setCategoria(t.getCategoria());
                n.setStatusEnvio(StatusEnvioNotificacao.ENVIADA);
                n.setDestinatario(idUsuario == null ? null : em.find(Usuario.class, idUsuario));
                n.setEmailDestino(email);
                n.setAssunto(renderizar(t.getAssunto(), variaveis));
                n.setTextoRenderizado(renderizar(t.getCorpoTemplate(), variaveis));
                notificacoes.persist(n);
                return new Object[] { n.getId(), n.getAssunto(), n.getTextoRenderizado() };
            });
            idNotificacao = (Long) r[0];
            assunto = (String) r[1];
            html = (String) r[2];
        } catch (RuntimeException e) {
            LOG.errorf(e, "Notificação %s para %s não registrada", codigo, email);
            return;
        }
        String identificador = null;
        StatusEnvioNotificacao status;
        try {
            identificador = resend.enviar(new ResendClient.NovoEmail(remetente, email, assunto, html)).id();
            status = StatusEnvioNotificacao.ENVIADA;
        } catch (RuntimeException e) {
            LOG.warnf("Integração Resend falhou para a notificação %d: %s", idNotificacao, e.getMessage());
            status = StatusEnvioNotificacao.FALHA;
        }
        String id = identificador;
        StatusEnvioNotificacao s = status;
        QuarkusTransaction.requiringNew().run(() -> {
            Notificacao n = notificacoes.findById(idNotificacao);
            n.setStatusEnvio(s);
            n.setIdentificadorProvedor(id);
        });
    }
}

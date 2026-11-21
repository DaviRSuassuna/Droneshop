package br.com.droneshop.notificacao.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.notificacao.dto.PreferenciaNotificacaoDTO;
import br.com.droneshop.notificacao.model.PreferenciaNotificacao;
import br.com.droneshop.notificacao.repository.PreferenciaNotificacaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PreferenciaNotificacaoServiceImpl implements PreferenciaNotificacaoService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Inject
    PreferenciaNotificacaoRepository preferencias;

    @Inject
    EntityManager em;

    @Override
    @Transactional
    public List<PreferenciaNotificacaoDTO> doUsuario(Long idUsuario) {
        List<PreferenciaNotificacao> salvas = preferencias.doUsuario(idUsuario);
        return PreferenciaNotificacao.TIPOS.stream().map(t -> new PreferenciaNotificacaoDTO(t,
                salvas.stream().anyMatch(p -> p.getTipoNotificacao().equals(t) && p.isHabilitado()))).toList();
    }

    @Override
    @Transactional
    public void salvar(Long idUsuario, List<PreferenciaNotificacaoDTO> lista) {
        if (lista == null)
            return;
        for (PreferenciaNotificacaoDTO dto : lista)
            if (dto == null || dto.tipoNotificacao() == null
                    || !PreferenciaNotificacao.TIPOS.contains(dto.tipoNotificacao().trim()))
                throw ValidationException.of("tipoNotificacao",
                        "Tipo de notificação inexistente ou transacional (back)");
        for (PreferenciaNotificacaoDTO dto : lista) {
            String tipo = dto.tipoNotificacao().trim();
            PreferenciaNotificacao p = preferencias.doUsuario(idUsuario, tipo).orElseGet(() -> {
                PreferenciaNotificacao nova = new PreferenciaNotificacao();
                nova.setUsuario(em.getReference(Usuario.class, idUsuario));
                nova.setTipoNotificacao(tipo);
                nova.setTokenDescadastro(token());
                preferencias.persist(nova);
                return nova;
            });
            p.setHabilitado(Boolean.TRUE.equals(dto.habilitado()));
        }
    }

    @Override
    @Transactional
    public PreferenciaNotificacaoDTO descadastrar(String token) {
        PreferenciaNotificacao p = preferencias.porToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Link de descadastro inválido."));
        p.setHabilitado(false);
        return new PreferenciaNotificacaoDTO(p.getTipoNotificacao(), false);
    }

    @Override
    @Transactional
    public String tokenSeHabilitado(Long idUsuario, String tipo) {
        return preferencias.doUsuario(idUsuario, tipo).filter(PreferenciaNotificacao::isHabilitado)
                .map(PreferenciaNotificacao::getTokenDescadastro).orElse(null);
    }

    private static String token() {
        byte[] b = new byte[24];
        ALEATORIO.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}

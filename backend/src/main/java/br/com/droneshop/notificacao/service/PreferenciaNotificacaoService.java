package br.com.droneshop.notificacao.service;

import java.util.List;

import br.com.droneshop.notificacao.dto.PreferenciaNotificacaoDTO;

public interface PreferenciaNotificacaoService {

    /** Todos os tipos configuráveis; sem escolha, desabilitado (opt-in, RN10). */
    List<PreferenciaNotificacaoDTO> doUsuario(Long idUsuario);

    /** 400 em tipoNotificacao quando o tipo não existe ou é transacional. */
    void salvar(Long idUsuario, List<PreferenciaNotificacaoDTO> preferencias);

    /** Desabilita o tipo do token; 404 com token desconhecido. */
    PreferenciaNotificacaoDTO descadastrar(String token);

    /** Token do link de descadastro, ou null quando o usuário não deu opt-in no tipo. */
    String tokenSeHabilitado(Long idUsuario, String tipo);
}

package br.com.droneshop.notificacao.service;

import java.util.Map;

import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.notificacao.dto.NotificacaoResponseDTO;
import br.com.droneshop.notificacao.dto.TemplateEmailRequestDTO;
import br.com.droneshop.notificacao.dto.TemplateEmailResponseDTO;
import br.com.droneshop.notificacao.dto.WebhookResendDTO;

public interface NotificacaoService {

    java.util.List<TemplateEmailResponseDTO> templates();

    TemplateEmailResponseDTO template(String codigo);

    /** Editar cria nova versão (versao + 1) e desativa a anterior (RF39). */
    TemplateEmailResponseDTO editar(String codigo, TemplateEmailRequestDTO dto);

    PageResponse<NotificacaoResponseDTO> listar(String destinatario, Long idStatusEnvio, int page, int pageSize);

    void webhook(WebhookResendDTO evento);

    /**
     * Renderiza e envia o e-mail fora da transação de quem disparou: grava a notificação
     * numa transação própria e, se o provedor falhar, marca FALHA sem lançar (RNF05, RNF10).
     *
     * @param idUsuario destinatário com conta; nulo para contato sem conta
     */
    void enviar(String codigo, String email, Long idUsuario, Map<String, String> variaveis);
}

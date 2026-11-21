package br.com.droneshop.notificacao.dto;

import java.time.LocalDateTime;

import br.com.droneshop.notificacao.model.CategoriaNotificacao;
import br.com.droneshop.notificacao.model.Notificacao;
import br.com.droneshop.notificacao.model.StatusEnvioNotificacao;

public record NotificacaoResponseDTO(Long id, Template template, CategoriaNotificacao categoria,
        StatusEnvioNotificacao statusEnvio, Destinatario destinatario, String assunto, String identificadorProvedor,
        LocalDateTime dataCadastro) {

    public record Template(String codigo, int versao) {
    }

    public record Destinatario(String email) {
    }

    public static NotificacaoResponseDTO from(Notificacao n) {
        return new NotificacaoResponseDTO(n.getId(),
                new Template(n.getTemplate().getCodigo(), n.getTemplate().getVersao()), n.getCategoria(),
                n.getStatusEnvio(), new Destinatario(n.getEmailDestino()), n.getAssunto(),
                n.getIdentificadorProvedor(), n.getDataCadastro());
    }
}

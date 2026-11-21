package br.com.droneshop.atendimento.service;

/** Evento disparado quando o atendente responde; a notificação manda CHAMADO_RESPONDIDO depois do commit. */
public record ChamadoRespondido(Long idChamado) {
}

package br.com.droneshop.compra.service;

/**
 * Evento CDI disparado quando a rotina marca o carrinho como ABANDONADO. A notificação (#42)
 * reage depois do commit e manda o e-mail de recuperação a quem deu opt-in.
 */
public record CarrinhoAbandonado(Long idCarrinho) {
}

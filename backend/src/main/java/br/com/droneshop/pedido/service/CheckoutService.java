package br.com.droneshop.pedido.service;

import br.com.droneshop.pedido.dto.CheckoutRequestDTO;
import br.com.droneshop.pedido.model.Pedido;

public interface CheckoutService {

    Pedido fechar(Long idUsuario, String tokenCarrinho, CheckoutRequestDTO dto);
}

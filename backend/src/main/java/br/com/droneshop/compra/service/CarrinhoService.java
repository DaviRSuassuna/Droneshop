package br.com.droneshop.compra.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.droneshop.compra.dto.CarrinhoResponseDTO;
import br.com.droneshop.compra.dto.ItemCarrinhoRequestDTO;
import br.com.droneshop.compra.model.Carrinho;

public interface CarrinhoService {

    CarrinhoResponseDTO criar();

    CarrinhoResponseDTO buscar(String token);

    CarrinhoResponseDTO adicionar(String token, ItemCarrinhoRequestDTO dto);

    CarrinhoResponseDTO alterarQuantidade(String token, Long idItem, int quantidade);

    CarrinhoResponseDTO remover(String token, Long idItem);

    CarrinhoResponseDTO vincular(Long idUsuario, String token);

    CarrinhoResponseDTO doCliente(Long idUsuario);

    CarrinhoResponseDTO reservar(String token);

    CarrinhoResponseDTO aplicarCupom(String token, String codigo, Long idUsuario);

    CarrinhoResponseDTO removerCupom(String token);

    int marcarAbandonados(LocalDateTime referencia);

    /** Carrinho ATIVO pelo token (404 se não houver, 409 se não estiver ativo), para o checkout. */
    Carrinho ativo(String token);

    /** Desconto do cupom aplicado, conferindo o limite do cliente (checkout, #26 e #29). */
    BigDecimal desconto(Carrinho c, Long idUsuario);

    CarrinhoResponseDTO resposta(Carrinho c);
}

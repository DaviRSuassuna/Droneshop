package br.com.droneshop.compra.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.compra.model.StatusCarrinho;
import br.com.droneshop.estoque.model.StatusReserva;

public record CarrinhoResponseDTO(String token, StatusCarrinho status, List<Item> itens, BigDecimal subtotal,
        BigDecimal desconto, BigDecimal total, CupomAplicado cupom, List<String> avisos) {

    public record Produto(Long id, String sku, String nome, String slug) {
    }

    public record ProdutoRef(Long id) {
    }

    public record Reserva(Long id, ProdutoRef produto, int quantidade, StatusReserva status, LocalDateTime expiraEm) {
    }

    public record Item(Long id, Produto produto, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal,
            boolean disponivel, List<Reserva> reservas) {
    }

    public record CupomAplicado(String codigo, BigDecimal desconto) {
    }
}

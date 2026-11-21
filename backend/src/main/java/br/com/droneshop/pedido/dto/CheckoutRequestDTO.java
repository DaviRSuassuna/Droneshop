package br.com.droneshop.pedido.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequestDTO(
        @NotNull(message = "Escolha o endereço (back)") Long idEndereco,
        @NotNull(message = "Escolha a entrega (back)") Long idModalidadeEntrega,
        String codigoServicoTransportadora,
        @NotNull(message = "Escolha o pagamento (back)") Long idMetodoPagamento) {
}

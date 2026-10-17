package br.com.droneshop.compra.dto;

import java.time.LocalDateTime;

public record UsoCupomResponseDTO(
        Long id,
        PedidoResumo pedido,
        ClienteResumo cliente,
        LocalDateTime dataUso
) {
    public record PedidoResumo(Long id) {}
    public record ClienteResumo(Long id, String nome) {}
}

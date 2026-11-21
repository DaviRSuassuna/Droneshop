package br.com.droneshop.estoque.dto;

import java.time.LocalDateTime;

import br.com.droneshop.estoque.model.Reserva;
import br.com.droneshop.estoque.model.StatusReserva;

public record ReservaResponseDTO(Long id, Produto produto, int quantidade, StatusReserva status,
        LocalDateTime expiraEm) {

    public record Produto(Long id, String sku, String nome) {
    }

    public static ReservaResponseDTO de(Reserva r) {
        return new ReservaResponseDTO(r.getId(),
                new Produto(r.getProduto().getId(), r.getProduto().getSku(), r.getProduto().getNome()),
                r.getQuantidade(), r.getStatus(), r.getExpiraEm());
    }
}

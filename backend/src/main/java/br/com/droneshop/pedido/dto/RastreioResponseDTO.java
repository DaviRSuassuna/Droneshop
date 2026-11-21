package br.com.droneshop.pedido.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.pedido.model.Entrega;

public record RastreioResponseDTO(String codigoRastreio, String transportadora, List<Evento> eventos) {

    public record Evento(Long id, String descricao, String localizacao, LocalDateTime dataHora) {
    }

    public static RastreioResponseDTO de(Entrega e) {
        if (e == null)
            return new RastreioResponseDTO(null, null, List.of());
        return new RastreioResponseDTO(e.getCodigoRastreio(), e.getTransportadora(), e.getEventos().stream()
                .map(v -> new Evento(v.getId(), v.getDescricao(), v.getLocalizacao(), v.getDataHora())).toList());
    }
}

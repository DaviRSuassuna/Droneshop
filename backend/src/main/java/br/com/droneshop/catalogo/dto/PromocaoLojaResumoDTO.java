package br.com.droneshop.catalogo.dto;

import java.time.LocalDateTime;

public record PromocaoLojaResumoDTO(
        Long id,
        String nome,
        LocalDateTime dataFim
) {
}

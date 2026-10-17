package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.catalogo.model.EscopoPromocao;

public record PromocaoResponseDTO(
        Long id,
        String nome,
        EscopoPromocao escopo,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        BigDecimal percentualDesconto,
        BigDecimal valorDesconto,
        BigDecimal valorMinimoCarrinho,
        boolean ativa,
        String situacao,
        List<VariantePromocaoResponseDTO> variantes
) {
}

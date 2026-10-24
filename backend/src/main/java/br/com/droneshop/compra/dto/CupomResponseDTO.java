package br.com.droneshop.compra.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.compra.model.TipoDescontoCupom;

public record CupomResponseDTO(
        Long id,
        String codigo,
        TipoDescontoCupom tipoDesconto,
        BigDecimal valor,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        BigDecimal valorMinimo,
        Integer limiteTotalUso,
        Integer limitePorCliente,
        boolean ativo,
        String situacao,
        long usos,
        List<ReferenciaCupomDTO> categorias,
        List<ReferenciaCupomDTO> marcas,
        List<ReferenciaCupomDTO> produtos
) {
    public record ReferenciaCupomDTO(Long id, String nome, String sku) {
        public ReferenciaCupomDTO(Long id, String nome) {
            this(id, nome, null);
        }
    }
}

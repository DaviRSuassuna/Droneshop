package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

public record VarianteResponseDTO(
    Long id,
    String nome,
    String sku,
    BigDecimal preco,
    BigDecimal custoAquisicao,
    BigDecimal margemPercentual,
    Integer estoqueFisico,
    int estoqueMinimo,
    BigDecimal pesoEmbalagemKg,
    Integer alturaEmbalagemCm,
    Integer larguraEmbalagemCm,
    Integer comprimentoEmbalagemCm,
    boolean ehKit,
    boolean ativa
) {
}

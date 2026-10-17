package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

// No kit, peso e dimensoes ja vem derivados dos componentes, salvo sobrescrita (D16);
// pesoSobrescrito e dimensoesSobrescritas dizem se o valor foi informado no proprio kit.
public record VarianteResponseDTO(
    Long id,
    String nome,
    String sku,
    BigDecimal preco,
    BigDecimal custoAquisicao,
    BigDecimal margemPercentual,
    Integer estoqueFisico,
    int disponivel,
    int estoqueMinimo,
    BigDecimal pesoEmbalagemKg,
    Integer alturaEmbalagemCm,
    Integer larguraEmbalagemCm,
    Integer comprimentoEmbalagemCm,
    boolean pesoSobrescrito,
    boolean dimensoesSobrescritas,
    boolean ehKit,
    boolean ativa,
    List<ComponenteKitResponseDTO> componentes
) {
}

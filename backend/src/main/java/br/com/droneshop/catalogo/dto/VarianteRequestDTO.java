package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

// Linha da tabela de variantes do drone/acessorio (D13) e corpo de PUT /variantes/{id}.
// No PUT do produto, com id altera e sem id cria. estoqueFisico so vale na criacao.
// Kit (D7): sem estoqueFisico e com componentes; peso e dimensoes vazios sao derivados (D16).
public record VarianteRequestDTO(
    Long id,

    @Length(max = 60, message = "O campo deve ter no máximo 60 caracteres (back).")
    String nome,

    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    String sku,

    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero (back).")
    BigDecimal preco,

    @DecimalMin(value = "0.0", message = "O custo não pode ser negativo (back).")
    BigDecimal custoAquisicao,

    @Min(value = 0, message = "O estoque não pode ser negativo (back).")
    Integer estoqueFisico,

    @Min(value = 0, message = "O estoque mínimo não pode ser negativo (back).")
    Integer estoqueMinimo,

    @DecimalMin(value = "0.0", inclusive = false, message = "O peso deve ser maior que zero (back).")
    BigDecimal pesoEmbalagemKg,

    @Min(value = 1, message = "A medida deve ser maior que zero (back).")
    Integer alturaEmbalagemCm,

    @Min(value = 1, message = "A medida deve ser maior que zero (back).")
    Integer larguraEmbalagemCm,

    @Min(value = 1, message = "A medida deve ser maior que zero (back).")
    Integer comprimentoEmbalagemCm,

    // nulo mantem o da variante salva; na criacao, nulo e false
    Boolean ehKit,

    List<@Valid ComponenteKitRequestDTO> componentes,

    Boolean ativa
) {
    public VarianteRequestDTO {
        nome = Textos.nuloSeVazio(nome);
        sku = Textos.nuloSeVazio(sku);
    }
}

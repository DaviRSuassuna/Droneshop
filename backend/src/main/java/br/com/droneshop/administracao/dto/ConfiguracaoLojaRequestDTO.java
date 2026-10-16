package br.com.droneshop.administracao.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ConfiguracaoLojaRequestDTO(
    @NotBlank(message = "Informe os dados fiscais da loja (back).")
    @Length(max = 2000, message = "Os dados fiscais devem ter no máximo 2000 caracteres (back).")
    String dadosFiscais,

    @NotBlank(message = "Informe o CEP de origem do frete (back).")
    @Pattern(regexp = "^\\d{8}$", message = "O CEP deve conter exatamente 8 dígitos (back).")
    String cepOrigemFrete,

    @NotBlank(message = "Informe o remetente padrão (back).")
    @Length(max = 150, message = "O remetente padrão deve ter no máximo 150 caracteres (back).")
    String remetentePadrao,

    @NotNull(message = "Informe o prazo de arrependimento (back).")
    @Min(value = 7, message = "O prazo de arrependimento deve ser de pelo menos 7 dias (back).")
    Integer prazoArrependimentoDias,

    @NotNull(message = "Informe o prazo de garantia (back).")
    @Min(value = 1, message = "O prazo de garantia deve ser de pelo menos 1 dia (back).")
    Integer prazoGarantiaDias,

    @NotNull(message = "Informe o prazo de análise da devolução (back).")
    @Min(value = 1, message = "O prazo de análise deve ser de pelo menos 1 dia útil (back).")
    Integer prazoAnaliseDevolucaoDiasUteis,

    @NotNull(message = "Informe quem paga o frete reverso (back).")
    Boolean freteReversoPorContaLoja
) {
}

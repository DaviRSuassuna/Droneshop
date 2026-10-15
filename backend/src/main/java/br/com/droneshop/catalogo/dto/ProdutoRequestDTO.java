package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProdutoRequestDTO(
    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 2, max = 120, message = "O campo deve conter entre 2 e 120 caracteres (back).")
    String nome,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    String modelo,

    @Length(max = 300, message = "O campo deve ter no máximo 300 caracteres (back).")
    String descricaoCurta,

    @NotNull(message = "O campo deve ser informado (back).")
    @DecimalMin(value = "0.0", message = "O preço não pode ser negativo (back).")
    BigDecimal preco,

    @NotNull(message = "Selecione a marca (back).")
    Long idMarca,

    @NotNull(message = "Selecione a subcategoria (back).")
    Long idSubcategoria,

    @NotNull(message = "Selecione o fornecedor (back).")
    Long idFornecedor,

    @NotNull(message = "O status deve ser informado (back).")
    Long idStatus,

    Boolean destaque
) {
}

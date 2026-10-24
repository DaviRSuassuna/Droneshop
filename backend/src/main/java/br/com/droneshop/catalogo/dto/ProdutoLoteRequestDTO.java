package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProdutoLoteRequestDTO(
    @NotEmpty(message = "Selecione os produtos (back).")
    List<@NotNull(message = "Informe o produto (back).") @Positive(message = "Produto inválido (back).") Long> ids,
    @NotNull(message = "Informe a ação (back).")
    @Size(max = 30, message = "A ação deve ter no máximo 30 caracteres (back).")
    @Pattern(regexp = "ATIVAR|INATIVAR|MOVER_SUBCATEGORIA|REAJUSTAR_PRECO", message = "Ação inválida (back).")
    String acao,
    Long idSubcategoria,
    BigDecimal percentual
) {}

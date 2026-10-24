package br.com.droneshop.catalogo.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CategoriaOrdemRequestDTO(
    @NotEmpty(message = "Informe a ordem das categorias (back).")
    List<@NotNull(message = "Informe a categoria (back).") @Positive(message = "Categoria inválida (back).") Long> ids
) {}

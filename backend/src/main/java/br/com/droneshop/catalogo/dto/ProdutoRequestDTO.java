package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

// Sem @NotBlank/@NotNull: rascunho salva so com o status (D28), entao a obrigatoriedade
// (inclusive do idStatus) fica no ProdutoServiceImpl. Texto em branco vira null no construtor.
public record ProdutoRequestDTO(
    @Length(min = 2, max = 120, message = "O campo deve conter entre 2 e 120 caracteres (back).")
    String nome,

    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    String modelo,

    @Length(max = 300, message = "O campo deve ter no máximo 300 caracteres (back).")
    String descricaoCurta,

    @Length(max = 5000, message = "O campo deve ter no máximo 5000 caracteres (back).")
    String descricao,

    @Length(max = 140, message = "O campo deve ter no máximo 140 caracteres (back).")
    String slug,

    @Length(max = 70, message = "O campo deve ter no máximo 70 caracteres (back).")
    String metaTitle,

    @Length(max = 160, message = "O campo deve ter no máximo 160 caracteres (back).")
    String metaDescription,

    @Length(max = 255, message = "O campo deve ter no máximo 255 caracteres (back).")
    String tags,

    // vazio herda o padrao da configuracao da loja (D15)
    @Min(value = 0, message = "A garantia não pode ser negativa (back).")
    Integer garantiaMeses,

    LocalDate dataLancamento,

    @DecimalMin(value = "0.0", message = "O preço não pode ser negativo (back).")
    BigDecimal preco,

    Long idMarca,

    Long idSubcategoria,

    Long idFornecedor,

    Long idStatus,

    Boolean destaque
) {
    public ProdutoRequestDTO {
        nome = Textos.nuloSeVazio(nome);
        modelo = Textos.nuloSeVazio(modelo);
        descricaoCurta = Textos.nuloSeVazio(descricaoCurta);
        descricao = Textos.nuloSeVazio(descricao);
        slug = Textos.nuloSeVazio(slug);
        metaTitle = Textos.nuloSeVazio(metaTitle);
        metaDescription = Textos.nuloSeVazio(metaDescription);
        tags = Textos.nuloSeVazio(tags);
    }
}

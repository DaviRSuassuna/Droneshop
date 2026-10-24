package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

// Kit (D7, D40): sem estoque proprio. estoqueFisico so existe aqui para responder 400 se
// vier preenchido; peso e dimensoes vazios sao derivados dos componentes (D16).
public record KitRequestDTO(
    @Length(min = 2, max = 120, message = "O campo deve conter entre 2 e 120 caracteres (back).")
    String nome,

    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    String sku,

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

    @Min(value = 0, message = "A garantia não pode ser negativa (back).")
    Integer garantiaMeses,

    LocalDate dataLancamento,

    Long idMarca,

    Long idSubcategoria,

    Long idFornecedor,

    Long idStatus,

    Boolean destaque,

    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero (back).")
    BigDecimal preco,

    @DecimalMin(value = "0.0", message = "O custo não pode ser negativo (back).")
    BigDecimal custoAquisicao,

    Integer estoqueFisico,

    @DecimalMin(value = "0.0", inclusive = false, message = "O peso deve ser maior que zero (back).")
    BigDecimal pesoEmbalagemKg,

    @Min(value = 1, message = "A medida deve ser maior que zero (back).")
    Integer alturaEmbalagemCm,

    @Min(value = 1, message = "A medida deve ser maior que zero (back).")
    Integer larguraEmbalagemCm,

    @Min(value = 1, message = "A medida deve ser maior que zero (back).")
    Integer comprimentoEmbalagemCm,

    Long idGrupo,

    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    String rotuloNoGrupo,

    @Min(value = 0, message = "A ordem não pode ser negativa (back).")
    Integer ordemNoGrupo,

    // validados item a item no ProdutoCadastro, para o erro sair como componentes[i].campo
    List<ComponenteKitRequestDTO> componentes
) implements ProdutoRequestDTO {
    public KitRequestDTO {
        nome = Textos.nuloSeVazio(nome);
        sku = Textos.nuloSeVazio(sku);
        rotuloNoGrupo = Textos.nuloSeVazio(rotuloNoGrupo);
        descricaoCurta = Textos.nuloSeVazio(descricaoCurta);
        descricao = Textos.nuloSeVazio(descricao);
        slug = Textos.nuloSeVazio(slug);
        metaTitle = Textos.nuloSeVazio(metaTitle);
        metaDescription = Textos.nuloSeVazio(metaDescription);
        tags = Textos.nuloSeVazio(tags);
    }
}

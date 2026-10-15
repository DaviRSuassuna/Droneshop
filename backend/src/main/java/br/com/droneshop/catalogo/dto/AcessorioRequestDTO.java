package br.com.droneshop.catalogo.dto;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.constraints.Min;

public record AcessorioRequestDTO(
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

    @Min(value = 0, message = "A garantia não pode ser negativa (back).")
    Integer garantiaMeses,

    LocalDate dataLancamento,

    Long idMarca,

    Long idSubcategoria,

    Long idFornecedor,

    Long idStatus,

    Boolean destaque,

    @Length(max = 60, message = "O campo deve ter no máximo 60 caracteres (back).")
    String tipoAcessorio,

    // validadas item a item no ProdutoCadastro, para o erro sair como variantes[i].campo
    List<VarianteRequestDTO> variantes
) implements ProdutoRequestDTO {
    public AcessorioRequestDTO {
        nome = Textos.nuloSeVazio(nome);
        modelo = Textos.nuloSeVazio(modelo);
        descricaoCurta = Textos.nuloSeVazio(descricaoCurta);
        descricao = Textos.nuloSeVazio(descricao);
        slug = Textos.nuloSeVazio(slug);
        metaTitle = Textos.nuloSeVazio(metaTitle);
        metaDescription = Textos.nuloSeVazio(metaDescription);
        tags = Textos.nuloSeVazio(tags);
        tipoAcessorio = Textos.nuloSeVazio(tipoAcessorio);
    }
}

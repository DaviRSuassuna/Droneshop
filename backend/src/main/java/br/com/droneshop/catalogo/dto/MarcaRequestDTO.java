package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.constraints.NotNull;

// nome, paisOrigem e logoUrl nao tem @NotBlank: so sao obrigatorios fora de RASCUNHO,
// e quem valida isso e o MarcaServiceImpl. Texto em branco vira null no construtor
// (Textos.nuloSeVazio), senao o @Length reprovaria o rascunho vazio que a tela envia.
public record MarcaRequestDTO(
    @Length(min = 2, max = 80, message = "O campo deve conter entre 2 e 80 caracteres (back).")
    String nome,

    @Length(max = 90, message = "O campo deve ter no máximo 90 caracteres (back).")
    String slug,

    @Length(min = 2, max = 60, message = "O campo deve conter entre 2 e 60 caracteres (back).")
    String paisOrigem,

    @Length(max = 255, message = "O campo deve ter no máximo 255 caracteres (back).")
    String siteOficial,

    @Length(max = 2000, message = "O campo deve ter no máximo 2000 caracteres (back).")
    String descricao,

    @Length(max = 255, message = "O campo deve ter no máximo 255 caracteres (back).")
    String logoUrl,

    @NotNull(message = "O status deve ser informado (back).")
    Long idStatus,

    Boolean destacarHome
) {
    public MarcaRequestDTO {
        nome = Textos.nuloSeVazio(nome);
        slug = Textos.nuloSeVazio(slug);
        paisOrigem = Textos.nuloSeVazio(paisOrigem);
        siteOficial = Textos.nuloSeVazio(siteOficial);
        descricao = Textos.nuloSeVazio(descricao);
        logoUrl = Textos.nuloSeVazio(logoUrl);
    }
}

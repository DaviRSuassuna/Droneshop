package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.constraints.NotNull;

// O grupo nao tem rascunho: so ativo ou inativo. Slug vazio sai do nome.
public record GrupoProdutoRequestDTO(
    @NotNull(message = "O nome deve ser informado (back).")
    @Length(min = 2, max = 120, message = "O campo deve conter entre 2 e 120 caracteres (back).")
    String nome,

    @Length(max = 140, message = "O campo deve ter no máximo 140 caracteres (back).")
    String slug,

    @Length(max = 300, message = "O campo deve ter no máximo 300 caracteres (back).")
    String descricaoCurta,

    Boolean ativo
) {
    public GrupoProdutoRequestDTO {
        nome = Textos.nuloSeVazio(nome);
        slug = Textos.nuloSeVazio(slug);
        descricaoCurta = Textos.nuloSeVazio(descricaoCurta);
    }
}

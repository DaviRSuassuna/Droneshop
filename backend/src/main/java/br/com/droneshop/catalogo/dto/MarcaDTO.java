package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;

import jakarta.validation.constraints.NotBlank;

public final class MarcaDTO {

    // nome, paisOrigem e logoUrl NAO tem @NotBlank aqui: obrigatoriedade
    // depende do status (RASCUNHO nao exige, ATIVA/INATIVA exige) e quem
    // valida isso e o MarcaServiceImpl, nao o Bean Validation.
    // Texto em branco chega aqui como null (Textos.nuloSeVazio), senao o
    // @Length reprovaria o rascunho vazio que a tela envia.

    @Length(min = 2, max = 80, message = "O campo deve conter entre 2 e 80 caracteres (back).")
    private final String nome;

    @Length(max = 90, message = "O campo deve ter no máximo 90 caracteres (back).")
    private final String slug;

    @Length(min = 2, max = 60, message = "O campo deve conter entre 2 e 60 caracteres (back).")
    private final String paisOrigem;

    @Length(max = 255, message = "O campo deve ter no máximo 255 caracteres (back).")
    private final String siteOficial;

    @Length(max = 2000, message = "O campo deve ter no máximo 2000 caracteres (back).")
    private final String descricao;

    @Length(max = 255, message = "O campo deve ter no máximo 255 caracteres (back).")
    private final String logoUrl;

    // valor esperado: "RASCUNHO", "ATIVA" ou "INATIVA"
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String status;

    private final Boolean destacarHome;

    public MarcaDTO(String nome, String slug, String paisOrigem, String siteOficial,
                     String descricao, String logoUrl, String status, Boolean destacarHome) {
        this.nome = Textos.nuloSeVazio(nome);
        this.slug = Textos.nuloSeVazio(slug);
        this.paisOrigem = Textos.nuloSeVazio(paisOrigem);
        this.siteOficial = Textos.nuloSeVazio(siteOficial);
        this.descricao = Textos.nuloSeVazio(descricao);
        this.logoUrl = Textos.nuloSeVazio(logoUrl);
        this.status = status;
        this.destacarHome = destacarHome;
    }

    public String getNome() {
        return nome;
    }

    public String getSlug() {
        return slug;
    }

    public String getPaisOrigem() {
        return paisOrigem;
    }

    public String getSiteOficial() {
        return siteOficial;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getDestacarHome() {
        return destacarHome;
    }
}

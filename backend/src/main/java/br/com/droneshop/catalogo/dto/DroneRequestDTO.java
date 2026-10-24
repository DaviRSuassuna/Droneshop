package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import br.com.droneshop.util.Textos;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public record DroneRequestDTO(
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

    // so vale na criacao (saldo inicial); depois, so muda por movimento (#13)
    @Min(value = 0, message = "O estoque não pode ser negativo (back).")
    Integer estoqueFisico,

    @Min(value = 0, message = "O estoque mínimo não pode ser negativo (back).")
    Integer estoqueMinimo,

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

    @Min(value = 0, message = "O valor não pode ser negativo (back).")
    Integer pesoGramas,

    @Length(max = 30, message = "O campo deve ter no máximo 30 caracteres (back).")
    String dimensoesDobrado,

    @Length(max = 30, message = "O campo deve ter no máximo 30 caracteres (back).")
    String dimensoesAberto,

    @Min(value = 0, message = "O valor não pode ser negativo (back).")
    Integer autonomiaMinutos,

    @Min(value = 0, message = "O valor não pode ser negativo (back).")
    Integer alcanceMetros,

    @Min(value = 0, message = "O valor não pode ser negativo (back).")
    Integer velocidadeMaximaKmh,

    @Min(value = 0, message = "O valor não pode ser negativo (back).")
    Integer resolucaoFotoMp,

    @Length(max = 60, message = "O campo deve ter no máximo 60 caracteres (back).")
    String tipoSensor,

    @Length(max = 30, message = "O campo deve ter no máximo 30 caracteres (back).")
    String resolucaoVideo,

    @Length(max = 60, message = "O campo deve ter no máximo 60 caracteres (back).")
    String estabilizacao,

    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    String tipoBateria,

    @Min(value = 0, message = "O valor não pode ser negativo (back).")
    Integer capacidadeBateriaMah,

    @Min(value = 0, message = "O valor não pode ser negativo (back).")
    Integer tempoCarregamentoMinutos,

    Boolean gpsIntegrado,

    Boolean sensoresAnticolisao,

    Boolean exigeRegistroAnac,

    @Length(max = 30, message = "O campo deve ter no máximo 30 caracteres (back).")
    String classeAnac,

    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    String registroSisant
) implements ProdutoRequestDTO {
    public DroneRequestDTO {
        nome = Textos.nuloSeVazio(nome);
        sku = Textos.nuloSeVazio(sku);
        rotuloNoGrupo = Textos.nuloSeVazio(rotuloNoGrupo);
        descricaoCurta = Textos.nuloSeVazio(descricaoCurta);
        descricao = Textos.nuloSeVazio(descricao);
        slug = Textos.nuloSeVazio(slug);
        metaTitle = Textos.nuloSeVazio(metaTitle);
        metaDescription = Textos.nuloSeVazio(metaDescription);
        tags = Textos.nuloSeVazio(tags);
        dimensoesDobrado = Textos.nuloSeVazio(dimensoesDobrado);
        dimensoesAberto = Textos.nuloSeVazio(dimensoesAberto);
        tipoSensor = Textos.nuloSeVazio(tipoSensor);
        resolucaoVideo = Textos.nuloSeVazio(resolucaoVideo);
        estabilizacao = Textos.nuloSeVazio(estabilizacao);
        tipoBateria = Textos.nuloSeVazio(tipoBateria);
        classeAnac = Textos.nuloSeVazio(classeAnac);
        registroSisant = Textos.nuloSeVazio(registroSisant);
    }
}

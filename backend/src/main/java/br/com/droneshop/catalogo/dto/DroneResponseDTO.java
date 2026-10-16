package br.com.droneshop.catalogo.dto;

import java.time.LocalDate;
import java.util.List;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;

public record DroneResponseDTO(
    Long id,
    TipoProduto tipo,
    String nome,
    String modelo,
    String descricaoCurta,
    String descricao,
    String slug,
    String metaTitle,
    String metaDescription,
    String tags,
    int garantiaMeses,
    LocalDate dataLancamento,
    MarcaResponseDTO marca,
    SubcategoriaResponseDTO subcategoria,
    FornecedorResponseDTO fornecedor,
    StatusProduto status,
    boolean destaque,
    Integer pesoGramas,
    String dimensoesDobrado,
    String dimensoesAberto,
    Integer autonomiaMinutos,
    Integer alcanceMetros,
    Integer velocidadeMaximaKmh,
    Integer resolucaoFotoMp,
    String tipoSensor,
    String resolucaoVideo,
    String estabilizacao,
    String tipoBateria,
    Integer capacidadeBateriaMah,
    Integer tempoCarregamentoMinutos,
    boolean gpsIntegrado,
    boolean sensoresAnticolisao,
    boolean exigeRegistroAnac,
    String classeAnac,
    String registroSisant,
    List<VarianteResponseDTO> variantes,
    List<ProdutoResumoDTO> acessoriosCompativeis
) implements ProdutoResponseDTO {
}

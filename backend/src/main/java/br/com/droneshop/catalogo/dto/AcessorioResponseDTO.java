package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;

public record AcessorioResponseDTO(
    Long id,
    TipoProduto tipo,
    String nome,
    String sku,
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
    BigDecimal preco,
    BigDecimal custoAquisicao,
    BigDecimal margemPercentual,
    Integer estoqueFisico,
    int disponivel,
    int estoqueMinimo,
    BigDecimal pesoEmbalagemKg,
    Integer alturaEmbalagemCm,
    Integer larguraEmbalagemCm,
    Integer comprimentoEmbalagemCm,
    GrupoProdutoResumoDTO grupo,
    String rotuloNoGrupo,
    int ordemNoGrupo,
    String tipoAcessorio,
    Integer pesoItemG,
    boolean sugerirNosCompativeis,
    List<ProdutoResumoDTO> compativeis,
    List<ProdutoResumoDTO> acessoriosCompativeis,
    String imagemPrincipal
) implements ProdutoResponseDTO {
}

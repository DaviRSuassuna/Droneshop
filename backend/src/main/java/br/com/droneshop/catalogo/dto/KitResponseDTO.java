package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;

// No kit, peso e dimensoes ja vem derivados dos componentes, salvo sobrescrita (D16);
// pesoSobrescrito e dimensoesSobrescritas dizem se o valor foi informado no proprio kit.
// estoqueFisico e sempre nulo e disponivel vem dos componentes (D7).
public record KitResponseDTO(
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
    BigDecimal pesoEmbalagemKg,
    Integer alturaEmbalagemCm,
    Integer larguraEmbalagemCm,
    Integer comprimentoEmbalagemCm,
    GrupoProdutoResumoDTO grupo,
    String rotuloNoGrupo,
    int ordemNoGrupo,
    boolean pesoSobrescrito,
    boolean dimensoesSobrescritas,
    boolean exibirEmOfertas,
    List<ComponenteKitResponseDTO> componentes,
    List<ProdutoResumoDTO> acessoriosCompativeis
) implements ProdutoResponseDTO {
}

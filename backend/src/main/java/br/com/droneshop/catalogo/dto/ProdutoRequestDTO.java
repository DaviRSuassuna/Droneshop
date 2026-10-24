package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Campos comuns do corpo de POST/PUT de /drones, /acessorios e /kits. Cada record declara os
// campos com o Bean Validation; esta interface deixa o service tratar o comum uma vez so.
// Preco, estoque e embalagem sao do proprio produto (D40); estoqueFisico so vale na criacao.
public interface ProdutoRequestDTO {
    String nome();
    String sku();
    String descricaoCurta();
    String descricao();
    String slug();
    String metaTitle();
    String metaDescription();
    String tags();
    Integer garantiaMeses();
    LocalDate dataLancamento();
    Long idMarca();
    Long idSubcategoria();
    Long idFornecedor();
    Long idStatus();
    Boolean destaque();
    BigDecimal preco();
    BigDecimal custoAquisicao();
    BigDecimal pesoEmbalagemKg();
    Integer alturaEmbalagemCm();
    Integer larguraEmbalagemCm();
    Integer comprimentoEmbalagemCm();
    Long idGrupo();
    String rotuloNoGrupo();
    Integer ordemNoGrupo();

    // o kit nao tem estoque proprio (D7): o record dele nao declara estoqueMinimo
    default Integer estoqueFisico() {
        return null;
    }

    default Integer estoqueMinimo() {
        return null;
    }
}

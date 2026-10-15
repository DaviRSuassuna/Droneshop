package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Campos comuns do corpo de POST/PUT de /drones e /acessorios. Cada record declara os
// campos com o Bean Validation; esta interface deixa o service tratar o comum uma vez so.
public interface ProdutoRequestDTO {
    String nome();
    String modelo();
    String descricaoCurta();
    String descricao();
    String slug();
    String metaTitle();
    String metaDescription();
    String tags();
    Integer garantiaMeses();
    LocalDate dataLancamento();
    BigDecimal preco();
    Long idMarca();
    Long idSubcategoria();
    Long idFornecedor();
    Long idStatus();
    Boolean destaque();
}

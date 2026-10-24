package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;

// versoes na ordem do seletor; precoAPartirDe e o menor preco entre as versoes ATIVAS
// (nulo se nenhuma esta ativa), o mesmo "a partir de" do card na vitrine
public record GrupoProdutoResponseDTO(
    Long id,
    String nome,
    String slug,
    String descricaoCurta,
    boolean ativo,
    BigDecimal precoAPartirDe,
    List<Versao> versoes
) {
    public record Versao(Long idProduto, String nome, String sku, TipoProduto tipo, StatusProduto status,
            String rotulo, int ordem, BigDecimal preco) {
    }
}

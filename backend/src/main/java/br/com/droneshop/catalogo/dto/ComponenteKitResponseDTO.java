package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;

// O cadastro do kit mostra de cada componente o SKU e quanto ha disponivel
public record ComponenteKitResponseDTO(
    Long idProduto,
    String sku,
    String nome,
    TipoProduto tipo,
    StatusProduto status,
    Integer estoqueFisico,
    int disponivel,
    int quantidade
) {
}

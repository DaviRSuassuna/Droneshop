package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.TipoProduto;

// Resposta de /produtos: cada item sai com os campos do proprio tipo (drone traz
// as specs, acessorio traz tipoAcessorio, kit traz os componentes).
public sealed interface ProdutoResponseDTO permits DroneResponseDTO, AcessorioResponseDTO, KitResponseDTO {
    Long id();
    TipoProduto tipo();
}

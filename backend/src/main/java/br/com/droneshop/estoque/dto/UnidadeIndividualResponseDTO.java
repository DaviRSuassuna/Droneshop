package br.com.droneshop.estoque.dto;

import java.time.LocalDate;

import br.com.droneshop.estoque.model.StatusUnidade;
import br.com.droneshop.estoque.model.UnidadeIndividual;

public record UnidadeIndividualResponseDTO(Long id, Produto produto, String numeroSerie, String registroAnac,
        StatusUnidade status, LocalDate validadeGarantia, ItemPedidoRef itemPedido) {

    public record Produto(Long id, String sku, String nome) {
    }

    public record ItemPedidoRef(Long id, Long idPedido) {
    }

    public static UnidadeIndividualResponseDTO de(UnidadeIndividual u) {
        var p = u.getProduto();
        var i = u.getItemPedido();
        return new UnidadeIndividualResponseDTO(u.getId(), new Produto(p.getId(), p.getSku(), p.getNome()),
                u.getNumeroSerie(), u.getRegistroAnac(), u.getStatus(), u.getValidadeGarantia(),
                i == null ? null : new ItemPedidoRef(i.getId(), i.getPedido().getId()));
    }
}

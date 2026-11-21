package br.com.droneshop.compra.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.droneshop.compra.model.ItemListaDesejos;

/** precoAoAdicionar e quedaDePreco: aviso de queda de preço (#86). */
public record ItemListaDesejosResponseDTO(ProdutoResumoDTO produto, LocalDateTime adicionadoEm,
        BigDecimal precoAoAdicionar, boolean quedaDePreco) {

    public static ItemListaDesejosResponseDTO de(ItemListaDesejos i) {
        BigDecimal antes = i.getPrecoAoAdicionar();
        BigDecimal agora = i.getProduto().getPreco();
        boolean caiu = antes != null && agora != null && agora.compareTo(antes) < 0;
        return new ItemListaDesejosResponseDTO(ProdutoResumoDTO.de(i.getProduto()), i.getAdicionadoEm(), antes, caiu);
    }
}

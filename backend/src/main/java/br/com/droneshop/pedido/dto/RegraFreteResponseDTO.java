package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;

import br.com.droneshop.pedido.model.ModalidadeEntrega;
import br.com.droneshop.pedido.model.RegraFrete;

public record RegraFreteResponseDTO(Long id, ModalidadeEntrega modalidade, BigDecimal custoFixo, int prazoDias,
        String cepOrigem) {

    public static RegraFreteResponseDTO de(RegraFrete r) {
        return new RegraFreteResponseDTO(r.getId(), r.getModalidade(), r.getCustoFixo(), r.getPrazoDias(),
                r.getCepOrigem());
    }
}

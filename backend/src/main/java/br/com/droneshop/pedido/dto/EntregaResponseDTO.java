package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;

import br.com.droneshop.pedido.model.Entrega;
import br.com.droneshop.pedido.model.ModalidadeEntrega;
import br.com.droneshop.pedido.model.OrigemCalculoFrete;

public record EntregaResponseDTO(ModalidadeEntrega modalidade, OrigemCalculoFrete origemCalculo,
        String transportadora, String codigoServicoTransportadora, BigDecimal custoCongelado, int prazoCongeladoDias,
        String codigoRastreio) {

    public static EntregaResponseDTO de(Entrega e) {
        return e == null ? null
                : new EntregaResponseDTO(e.getModalidade(), e.getOrigemCalculo(), e.getTransportadora(),
                        e.getCodigoServicoTransportadora(), e.getCustoCongelado(), e.getPrazoCongeladoDias(),
                        e.getCodigoRastreio());
    }
}

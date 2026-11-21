package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;

import br.com.droneshop.pedido.model.ModalidadeEntrega;
import br.com.droneshop.pedido.model.OrigemCalculoFrete;

public record CotacaoFreteResponseDTO(ModalidadeEntrega modalidade, OrigemCalculoFrete origemCalculo,
        String transportadora, String codigoServicoTransportadora, BigDecimal custo, int prazoDias) {
}

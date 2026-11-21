package br.com.droneshop.pagamento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.droneshop.pagamento.model.Estorno;
import br.com.droneshop.pagamento.model.StatusPagamento;

public record EstornoResponseDTO(Long id, BigDecimal valor, String motivo, LocalDateTime dataCadastro,
        PagamentoRef pagamento) {

    public record PagamentoRef(Long id, StatusPagamento status) {
    }

    public static EstornoResponseDTO de(Estorno e) {
        return new EstornoResponseDTO(e.getId(), e.getValor(), e.getMotivo(), e.getDataCadastro(),
                new PagamentoRef(e.getPagamento().getId(), e.getPagamento().getStatus()));
    }
}

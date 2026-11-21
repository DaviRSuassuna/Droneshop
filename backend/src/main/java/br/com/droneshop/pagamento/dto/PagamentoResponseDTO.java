package br.com.droneshop.pagamento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.droneshop.pagamento.model.MetodoPagamento;
import br.com.droneshop.pagamento.model.Pagamento;
import br.com.droneshop.pagamento.model.StatusPagamento;

public record PagamentoResponseDTO(Long id, MetodoPagamento metodo, StatusPagamento status, BigDecimal valor,
        boolean vigente, String gatewayTransacaoId, LocalDateTime dataCadastro) {

    public static PagamentoResponseDTO de(Pagamento p) {
        return new PagamentoResponseDTO(p.getId(), p.getMetodo(), p.getStatus(), p.getValor(), p.isVigente(),
                p.getGatewayTransacaoId(), p.getDataCadastro());
    }
}

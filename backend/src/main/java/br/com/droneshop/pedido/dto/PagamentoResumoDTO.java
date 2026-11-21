package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.droneshop.pagamento.model.MetodoPagamento;
import br.com.droneshop.pagamento.model.Pagamento;
import br.com.droneshop.pagamento.model.StatusPagamento;

/** Pagamento como aparece dentro do pedido (#27). */
public record PagamentoResumoDTO(Long id, MetodoPagamento metodo, StatusPagamento status, BigDecimal valor,
        boolean vigente, String gatewayTransacaoId, LocalDateTime dataCadastro) {

    public static PagamentoResumoDTO de(Pagamento p) {
        return new PagamentoResumoDTO(p.getId(), p.getMetodo(), p.getStatus(), p.getValor(), p.isVigente(),
                p.getGatewayTransacaoId(), p.getDataCadastro());
    }
}

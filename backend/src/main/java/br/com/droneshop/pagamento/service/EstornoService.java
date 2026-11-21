package br.com.droneshop.pagamento.service;

import java.math.BigDecimal;
import java.util.List;

import br.com.droneshop.pagamento.model.Estorno;

public interface EstornoService {

    /** Estorno total ou parcial no gateway; a soma não passa do valor pago (RF25). */
    Estorno estornar(Long idPagamento, BigDecimal valor, String motivo);

    List<Estorno> doPagamento(Long idPagamento);
}

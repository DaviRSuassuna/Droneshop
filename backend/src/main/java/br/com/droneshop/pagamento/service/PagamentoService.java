package br.com.droneshop.pagamento.service;

import java.util.List;

import br.com.droneshop.pagamento.dto.PagamentoRequestDTO;
import br.com.droneshop.pagamento.model.Pagamento;

public interface PagamentoService {

    /** Nova tentativa vigente; cartão aprova ou recusa na hora, PIX e boleto esperam o webhook (#27). */
    Pagamento pagar(Long idUsuario, Long idPedido, PagamentoRequestDTO dto);

    /** Consulta o gateway e aplica o status; repetir não duplica nada. */
    void webhook(String gatewayTransacaoId);

    List<Pagamento> doPedido(Long idPedido);

    Pagamento buscar(Long id);
}

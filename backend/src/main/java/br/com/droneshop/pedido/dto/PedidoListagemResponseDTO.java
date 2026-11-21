package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.droneshop.pagamento.model.MetodoPagamento;
import br.com.droneshop.pedido.model.StatusPedido;

// Item de GET /pedidos; quantidadeItens = linhas do pedido, como na listagem do protótipo
public record PedidoListagemResponseDTO(
    Long id,
    String numero,
    StatusPedido status,
    ClientePedidoResponseDTO cliente,
    BigDecimal valorTotal,
    int quantidadeItens,
    LocalDateTime dataCadastro,
    // método do pagamento vigente (#89); null sem pagamento
    MetodoPagamento metodoPagamento
) {
}

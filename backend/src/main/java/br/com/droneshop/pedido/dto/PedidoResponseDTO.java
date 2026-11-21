package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.pagamento.model.MetodoPagamento;
import br.com.droneshop.pedido.model.StatusPedido;

// valorTotal = valorProdutos + valorFrete - valorDesconto; valorDesconto = soma de descontos[] (RN16)
public record PedidoResponseDTO(
    Long id,
    String numero,
    StatusPedido status,
    ClientePedidoResponseDTO cliente,
    BigDecimal valorProdutos,
    BigDecimal valorTotal,
    BigDecimal valorFrete,
    BigDecimal valorDesconto,
    List<ItemPedidoResponseDTO> itens,
    EnderecoPedidoResponseDTO enderecoEntrega,
    List<DescontoPedidoResponseDTO> descontos,
    MetodoPagamento metodoPagamento,
    List<PagamentoResumoDTO> pagamentos,
    EntregaResponseDTO entrega,
    LocalDateTime dataCadastro
) {
}

package br.com.droneshop.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.pedido.model.StatusPedido;

// pagamentos e entrega ficam vazios até as issues de pagamento (#27) e entrega (#30, #31)
public record PedidoResponseDTO(
    Long id,
    String numero,
    StatusPedido status,
    ClientePedidoResponseDTO cliente,
    BigDecimal valorTotal,
    BigDecimal valorFrete,
    BigDecimal valorDesconto,
    List<ItemPedidoResponseDTO> itens,
    EnderecoPedidoResponseDTO enderecoEntrega,
    List<Object> pagamentos,
    Object entrega,
    LocalDateTime dataCadastro
) {
}

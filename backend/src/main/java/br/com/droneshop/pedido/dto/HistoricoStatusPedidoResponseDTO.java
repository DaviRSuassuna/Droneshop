package br.com.droneshop.pedido.dto;

import java.time.LocalDateTime;

import br.com.droneshop.model.OrigemRegistro;
import br.com.droneshop.pedido.model.StatusPedido;

// autor nulo quando a origem não é MANUAL (D11)
public record HistoricoStatusPedidoResponseDTO(
    StatusPedido statusAnterior,
    StatusPedido statusNovo,
    OrigemRegistro origem,
    AutorResumo autor,
    String observacao,
    LocalDateTime dataCadastro
) {
    public record AutorResumo(Long id, String nome) {
    }
}

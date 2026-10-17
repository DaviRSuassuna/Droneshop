package br.com.droneshop.pedido.dto;

public record EnderecoPedidoResponseDTO(
    String destinatario,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String uf,
    String cep
) {
}

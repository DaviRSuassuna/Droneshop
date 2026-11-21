package br.com.droneshop.pedido.service;

public interface EtiquetaPedidoService {
    byte[] gerar(long idPedido);

    // #89: tudo ou nada; 404 se algum não existe, 409 citando os números sem endereço congelado
    byte[] gerarLote(java.util.List<Long> ids);
}

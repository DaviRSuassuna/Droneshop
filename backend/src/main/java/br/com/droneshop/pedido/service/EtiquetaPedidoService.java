package br.com.droneshop.pedido.service;

public interface EtiquetaPedidoService {
    byte[] gerar(long idPedido);
}

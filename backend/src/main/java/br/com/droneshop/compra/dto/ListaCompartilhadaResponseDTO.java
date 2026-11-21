package br.com.droneshop.compra.dto;

import java.util.List;

public record ListaCompartilhadaResponseDTO(String nomeCliente, List<Item> itens) {

    public record Item(ProdutoResumoDTO produto) {
    }
}

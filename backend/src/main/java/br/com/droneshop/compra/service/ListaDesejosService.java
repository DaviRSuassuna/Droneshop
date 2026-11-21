package br.com.droneshop.compra.service;

import java.util.List;

import br.com.droneshop.compra.dto.ItemListaDesejosResponseDTO;

public interface ListaDesejosService {

    List<ItemListaDesejosResponseDTO> listar(Long idUsuario);

    void adicionar(Long idUsuario, Long idProduto);

    void remover(Long idUsuario, Long idProduto);
}

package br.com.droneshop.catalogo.service;

import br.com.droneshop.catalogo.dto.ProdutoLoteRequestDTO;
import br.com.droneshop.catalogo.dto.ProdutoLoteResponseDTO;

public interface ProdutoLoteService {
    ProdutoLoteResponseDTO executar(ProdutoLoteRequestDTO dto);
}

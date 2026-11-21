package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.OfertaLojaResponseDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaResumoDTO;

public interface LojaComplementoService {

    List<ProdutoLojaResumoDTO> buscarCompativeis(String slug);

    List<OfertaLojaResponseDTO> buscarOfertas();
}

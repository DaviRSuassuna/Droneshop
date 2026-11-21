package br.com.droneshop.pedido.service;

import java.util.List;
import java.util.Map;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.pedido.dto.CotacaoFreteResponseDTO;
import br.com.droneshop.pedido.dto.RegraFreteRequestDTO;
import br.com.droneshop.pedido.model.RegraFrete;

public interface FreteService {

    /** EXPRESSA e PADRAO do agregador (ou da RegraFrete, se ele cair) e sempre a retirada (#30). */
    List<CotacaoFreteResponseDTO> cotar(String cep, Map<Produto, Integer> itens);

    RegraFrete criar(RegraFreteRequestDTO dto);

    RegraFrete alterar(Long id, RegraFreteRequestDTO dto);

    RegraFrete buscar(Long id);

    void excluir(Long id);
}

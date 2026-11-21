package br.com.droneshop.atendimento.service;

import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacaoRequestDTO;
import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacaoResponseDTO;
import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacoesProdutoDTO;
import br.com.droneshop.dto.PageResponse;

public interface AvaliacaoService {

    /** compraVerificada quando o cliente tem ItemPedido ENTREGUE do produto (RN04); 409 se já avaliou. */
    AvaliacaoResponseDTO avaliar(Long idUsuario, String slug, AvaliacaoRequestDTO dto);

    AvaliacoesProdutoDTO aprovadas(String slug);

    PageResponse<AvaliacaoResponseDTO> listar(Long idStatus, int page, int pageSize);

    void moderar(Long id, Long idStatus);
}

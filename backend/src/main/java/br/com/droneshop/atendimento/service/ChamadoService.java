package br.com.droneshop.atendimento.service;

import java.util.List;

import br.com.droneshop.atendimento.dto.ChamadoDTOs.ChamadoRequestDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.ChamadoResponseDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.MensagemResponseDTO;
import br.com.droneshop.dto.PageResponse;

public interface ChamadoService {

    ChamadoResponseDTO abrir(Long idUsuario, ChamadoRequestDTO dto);

    List<ChamadoResponseDTO> doCliente(Long idUsuario);

    List<MensagemResponseDTO> mensagensDoCliente(Long idUsuario, Long idChamado);

    MensagemResponseDTO responderComoCliente(Long idUsuario, Long idChamado, String texto);

    PageResponse<ChamadoResponseDTO> listar(Long idStatus, int page, int pageSize);

    ChamadoResponseDTO buscar(Long idChamado);

    /** Resposta do atendente: o chamado fica EM_ANDAMENTO e o cliente recebe CHAMADO_RESPONDIDO. */
    MensagemResponseDTO responderComoAtendente(Long idAtendente, Long idChamado, String texto);

    void alterarStatus(Long idChamado, Long idStatus);
}

package br.com.droneshop.atendimento.service;

import br.com.droneshop.atendimento.dto.LeadEmpresaDTOs.LeadEmpresaRequestDTO;
import br.com.droneshop.atendimento.dto.LeadEmpresaDTOs.LeadEmpresaResponseDTO;
import br.com.droneshop.dto.PageResponse;

public interface LeadEmpresaService {

    void criar(LeadEmpresaRequestDTO dto);

    PageResponse<LeadEmpresaResponseDTO> listar(Long idStatus, int page, int pageSize);

    void alterarStatus(Long id, Long idStatus);
}

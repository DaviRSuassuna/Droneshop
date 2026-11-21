package br.com.droneshop.atendimento.service;

import br.com.droneshop.atendimento.dto.LeadEmpresaDTOs.LeadEmpresaRequestDTO;
import br.com.droneshop.atendimento.dto.LeadEmpresaDTOs.LeadEmpresaResponseDTO;
import br.com.droneshop.atendimento.model.LeadEmpresa;
import br.com.droneshop.atendimento.model.StatusLead;
import br.com.droneshop.atendimento.repository.LeadEmpresaRepository;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Documentos;
import br.com.droneshop.util.Textos;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LeadEmpresaServiceImpl implements LeadEmpresaService {

    @Inject
    LeadEmpresaRepository leads;

    @Override
    @Transactional
    public void criar(LeadEmpresaRequestDTO dto) {
        LeadEmpresa l = new LeadEmpresa();
        l.setRazaoSocial(dto.razaoSocial().trim());
        l.setCnpj(Documentos.somenteDigitos(dto.cnpj()));
        l.setNomeContato(dto.nomeContato().trim());
        l.setEmailContato(dto.emailContato().trim().toLowerCase());
        l.setTelefoneContato(Documentos.somenteDigitosOuNulo(dto.telefoneContato()));
        l.setSegmento(Textos.nuloSeVazio(dto.segmento()));
        l.setQuantidadeEstimada(dto.quantidadeEstimada());
        l.setMensagem(Textos.nuloSeVazio(dto.mensagem()));
        l.setStatus(StatusLead.NOVO);
        leads.persist(l);
    }

    @Override
    @Transactional
    public PageResponse<LeadEmpresaResponseDTO> listar(Long idStatus, int page, int pageSize) {
        StatusLead status = StatusLead.valueOf(idStatus);
        var q = status == null ? leads.findAll(Sort.descending("id")) : leads.find("status", Sort.descending("id"), status);
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(), LeadEmpresaResponseDTO::from);
    }

    @Override
    @Transactional
    public void alterarStatus(Long id, Long idStatus) {
        LeadEmpresa l = leads.findById(id);
        if (l == null)
            throw new ResourceNotFoundException("Proposta não encontrada.");
        StatusLead novo = StatusLead.valueOf(idStatus);
        if (novo == null)
            throw ValidationException.of("idStatus", "Status inválido (back)");
        l.setStatus(novo);
    }
}

package br.com.droneshop.atendimento.resource;

import br.com.droneshop.atendimento.dto.LeadEmpresaDTOs.LeadEmpresaRequestDTO;
import br.com.droneshop.atendimento.service.LeadEmpresaService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

/** Formulário "Solicitar proposta" da página Empresas, público (D36). */
@Path("leads-empresas")
@PermitAll
@Consumes(MediaType.APPLICATION_JSON)
public class LeadEmpresaResource {

    @Inject
    LeadEmpresaService service;

    @POST
    public void criar(@Valid LeadEmpresaRequestDTO dto) {
        service.criar(dto);
    }
}

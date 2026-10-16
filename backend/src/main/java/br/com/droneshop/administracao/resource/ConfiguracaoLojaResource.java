package br.com.droneshop.administracao.resource;

import br.com.droneshop.administracao.dto.ConfiguracaoLojaRequestDTO;
import br.com.droneshop.administracao.dto.ConfiguracaoLojaResponseDTO;
import br.com.droneshop.administracao.service.ConfiguracaoLojaService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin/configuracao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfiguracaoLojaResource {

    @Inject
    ConfiguracaoLojaService service;

    @GET
    public ConfiguracaoLojaResponseDTO buscar() {
        return ConfiguracaoLojaResponseDTO.from(service.buscar());
    }

    @PUT
    public void atualizar(@Valid ConfiguracaoLojaRequestDTO dto) {
        service.atualizar(dto);
    }
}

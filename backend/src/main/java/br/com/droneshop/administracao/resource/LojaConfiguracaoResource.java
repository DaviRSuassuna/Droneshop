package br.com.droneshop.administracao.resource;

import br.com.droneshop.administracao.dto.ConfiguracaoLojaPublicaDTO;
import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.service.ConfiguracaoLojaService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Configuração da loja (#6) que a vitrine mostra (#21): sem dados fiscais. */
@PermitAll
@Path("loja/configuracao")
@Produces(MediaType.APPLICATION_JSON)
public class LojaConfiguracaoResource {

    @Inject
    ConfiguracaoLojaService service;

    @GET
    public ConfiguracaoLojaPublicaDTO buscar() {
        ConfiguracaoLoja c = service.buscar();
        return new ConfiguracaoLojaPublicaDTO(c.getPrazoArrependimentoDias(), c.isFreteReversoPorContaLoja());
    }
}

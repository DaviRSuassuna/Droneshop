package br.com.droneshop.identidade.resource;

import java.util.List;

import br.com.droneshop.identidade.dto.TermoResponseDTO;
import br.com.droneshop.identidade.service.LgpdService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Versões vigentes dos termos, para a tela de criar conta (#46). */
@Path("loja/termos")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
public class TermosResource {

    @Inject
    LgpdService lgpd;

    @GET
    public List<TermoResponseDTO> termos() {
        return lgpd.termos();
    }
}

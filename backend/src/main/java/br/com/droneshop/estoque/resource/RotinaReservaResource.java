package br.com.droneshop.estoque.resource;

import java.time.LocalDateTime;

import br.com.droneshop.dto.RotinaResponseDTO;
import br.com.droneshop.estoque.service.ReservaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Rotina sob demanda (D33): expira as reservas ATIVA vencidas antes da referência. */
@Path("admin/rotinas/expirar-reservas")
@RolesAllowed("ADMINISTRATIVO")
@Produces(MediaType.APPLICATION_JSON)
public class RotinaReservaResource {

    @Inject
    ReservaService service;

    @POST
    public RotinaResponseDTO expirar(@QueryParam("referencia") LocalDateTime referencia) {
        return new RotinaResponseDTO(service.expirarVencidas(referencia == null ? LocalDateTime.now() : referencia));
    }
}

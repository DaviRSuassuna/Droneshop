package br.com.droneshop.compra.resource;

import java.time.LocalDateTime;

import br.com.droneshop.compra.service.CarrinhoService;
import br.com.droneshop.dto.RotinaResponseDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Rotina sob demanda (D33): marca ABANDONADO o carrinho parado há mais de aeria.carrinho.abandono-horas. */
@Path("admin/rotinas/carrinhos-abandonados")
@RolesAllowed("ADMINISTRATIVO")
@Produces(MediaType.APPLICATION_JSON)
public class RotinaCarrinhoResource {

    @Inject
    CarrinhoService carrinhos;

    @POST
    public RotinaResponseDTO abandonados(@QueryParam("referencia") LocalDateTime referencia) {
        return new RotinaResponseDTO(carrinhos.marcarAbandonados(referencia == null ? LocalDateTime.now() : referencia));
    }
}

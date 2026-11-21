package br.com.droneshop.pedido.resource;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.pedido.dto.EventoRastreioRequestDTO;
import br.com.droneshop.pedido.dto.RastreioResponseDTO;
import br.com.droneshop.pedido.model.EventoRastreio;
import br.com.droneshop.pedido.service.RastreioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Eventos de rastreio lançados pelo admin (#31). */
@Path("pedidos/{id}/rastreio")
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.PEDIDO)
@Produces(MediaType.APPLICATION_JSON)
public class RastreioResource {

    @Inject
    RastreioService rastreio;

    @GET
    @Transactional
    public RastreioResponseDTO buscar(@PathParam("id") Long id) {
        return RastreioResponseDTO.de(rastreio.pedido(id).getEntrega());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public RastreioResponseDTO.Evento evento(@PathParam("id") Long id, @Valid EventoRastreioRequestDTO dto) {
        EventoRastreio e = rastreio.evento(id, dto.descricao(), dto.localizacao(), dto.dataHora());
        return new RastreioResponseDTO.Evento(e.getId(), e.getDescricao(), e.getLocalizacao(), e.getDataHora());
    }
}

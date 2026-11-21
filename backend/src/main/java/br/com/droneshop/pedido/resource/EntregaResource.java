package br.com.droneshop.pedido.resource;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.pedido.dto.CodigoRastreioRequestDTO;
import br.com.droneshop.pedido.service.RastreioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Código de rastreio da transportadora (#31). */
@Path("pedidos/{id}/entrega")
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.PEDIDO)
public class EntregaResource {

    @Inject
    RastreioService rastreio;

    @PUT
    @Path("codigo-rastreio")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response codigo(@PathParam("id") Long id, @Valid CodigoRastreioRequestDTO dto) {
        rastreio.codigo(id, dto.codigoRastreio());
        return Response.noContent().build();
    }
}

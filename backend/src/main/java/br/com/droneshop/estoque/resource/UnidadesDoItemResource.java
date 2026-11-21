package br.com.droneshop.estoque.resource;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.estoque.dto.VincularUnidadesRequestDTO;
import br.com.droneshop.estoque.service.UnidadeIndividualService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Expedição: liga as séries às unidades do item do pedido (RN14). */
@Path("pedidos/{id}/itens/{idItem}/unidades")
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.PEDIDO)
public class UnidadesDoItemResource {

    @Inject
    UnidadeIndividualService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response vincular(@PathParam("id") Long idPedido, @PathParam("idItem") Long idItem,
            VincularUnidadesRequestDTO dto) {
        service.vincular(idPedido, idItem, dto == null ? null : dto.idsUnidades());
        return Response.noContent().build();
    }
}

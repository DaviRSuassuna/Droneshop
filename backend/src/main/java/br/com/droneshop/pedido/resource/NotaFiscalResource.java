package br.com.droneshop.pedido.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.pedido.dto.NotaFiscalResponseDTO;
import br.com.droneshop.pedido.service.NotaFiscalService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** NF-e simulada do pedido (#35). */
@Path("pedidos/{id}/nota-fiscal")
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.PEDIDO)
@Produces(MediaType.APPLICATION_JSON)
public class NotaFiscalResource {

    @Inject
    NotaFiscalService notas;

    @POST
    public NotaFiscalResponseDTO emitir(@PathParam("id") Long id) {
        return NotaFiscalResponseDTO.de(notas.emitirVenda(id));
    }

    @GET
    @Transactional
    public List<NotaFiscalResponseDTO> listar(@PathParam("id") Long id) {
        return notas.doPedido(id).stream().map(NotaFiscalResponseDTO::de).toList();
    }
}

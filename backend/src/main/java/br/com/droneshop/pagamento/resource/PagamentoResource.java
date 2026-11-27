package br.com.droneshop.pagamento.resource;

import java.util.List;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.pagamento.dto.PagamentoRequestDTO;
import br.com.droneshop.pagamento.dto.PagamentoResponseDTO;
import br.com.droneshop.pagamento.service.PagamentoService;
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

/** Pagamentos do pedido: o cliente dono paga, o admin consulta (#27). */
@Path("pedidos/{id}/pagamentos")
@Produces(MediaType.APPLICATION_JSON)
public class PagamentoResource {

    @Inject
    UsuarioAtual usuarioAtual;

    @Inject
    PagamentoService pagamentos;

    @POST
    @RolesAllowed("CLIENTE")
    @Consumes(MediaType.APPLICATION_JSON)
    public PagamentoResponseDTO pagar(@PathParam("id") Long idPedido, @Valid PagamentoRequestDTO dto) {
        return PagamentoResponseDTO.de(pagamentos.pagar(usuarioAtual.id(), idPedido, dto));
    }

    @GET
    @RolesAllowed("ADMINISTRATIVO")
    @ModuloAdmin(ModuloSistema.PAGAMENTO)
    @Transactional
    public List<PagamentoResponseDTO> listar(@PathParam("id") Long idPedido) {
        return pagamentos.doPedido(idPedido).stream().map(PagamentoResponseDTO::de).toList();
    }
}

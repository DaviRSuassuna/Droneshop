package br.com.droneshop.pagamento.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.pagamento.dto.EstornoRequestDTO;
import br.com.droneshop.pagamento.dto.EstornoResponseDTO;
import br.com.droneshop.pagamento.service.EstornoService;
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

/** Estorno total ou parcial (#36). */
@Path("pagamentos/{id}")
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.PAGAMENTO)
@Produces(MediaType.APPLICATION_JSON)
public class EstornoResource {

    @Inject
    EstornoService estornos;

    @POST
    @Path("estorno")
    @Consumes(MediaType.APPLICATION_JSON)
    public EstornoResponseDTO estornar(@PathParam("id") Long id, @Valid EstornoRequestDTO dto) {
        return EstornoResponseDTO.de(estornos.estornar(id, dto.valor(), dto.motivo()));
    }

    @GET
    @Path("estornos")
    @Transactional
    public List<EstornoResponseDTO> listar(@PathParam("id") Long id) {
        return estornos.doPagamento(id).stream().map(EstornoResponseDTO::de).toList();
    }
}

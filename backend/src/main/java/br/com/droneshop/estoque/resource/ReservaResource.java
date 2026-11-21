package br.com.droneshop.estoque.resource;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.estoque.dto.ReservaResponseDTO;
import br.com.droneshop.estoque.repository.ReservaRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("estoque/reservas")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
public class ReservaResource {

    @Inject
    ReservaRepository reservas;

    // Só as ATIVA: é o que está prendendo saldo agora (#23)
    @GET
    @ModuloAdmin(ModuloSistema.ESTOQUE)
    @Transactional
    public PageResponse<ReservaResponseDTO> buscar(@QueryParam("idProduto") Long idProduto,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        var query = reservas.ativas(idProduto);
        return PageResponse.of(query.page(page, pageSize).list(), page, pageSize, query.count(),
                ReservaResponseDTO::de);
    }
}

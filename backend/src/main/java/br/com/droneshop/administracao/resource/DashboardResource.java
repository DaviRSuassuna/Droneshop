package br.com.droneshop.administracao.resource;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.dto.DashboardResponseDTO;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.administracao.service.DashboardService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Indicadores calculados no backend (#18); a tela só exibe
@Path("admin/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ADMINISTRACAO)
public class DashboardResource {

    @Inject
    DashboardService service;

    // datas sobre o dataCadastro do pedido, inclusivas (AAAA-MM-DD); sem datas, os últimos 30 dias
    @GET
    public DashboardResponseDTO buscar(@QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        return service.resumo(dataInicio, dataFim);
    }
}

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
import jakarta.ws.rs.core.Response;

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

    // CSV das vendas por dia, no padrão da #61 (separador ";", UTF-8 com BOM). Só datas e números,
    // então as células saem sem aspas, como no cabeçalho do contrato.
    @GET
    @Path("exportar")
    @Produces("text/csv")
    public Response exportar(@QueryParam("dataInicio") String dataInicio, @QueryParam("dataFim") String dataFim) {
        StringBuilder csv = new StringBuilder("\uFEFFdata;faturamento;pedidos;faturamentoAnterior;pedidosAnterior\r\n");
        for (DashboardResponseDTO.VendaDia v : service.vendasPorDia(dataInicio, dataFim))
            csv.append(v.data()).append(';').append(v.faturamento().toPlainString()).append(';')
                    .append(v.quantidadePedidos()).append(';').append(v.faturamentoAnterior().toPlainString())
                    .append(';').append(v.quantidadePedidosAnterior()).append("\r\n");
        return Response.ok(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=\"vendas-por-dia.csv\"").build();
    }
}

package br.com.droneshop.pagamento.resource;

import br.com.droneshop.pagamento.model.StatusPagamento;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@PermitAll
@Path("status-pagamento")
@Produces(MediaType.APPLICATION_JSON)
public class StatusPagamentoResource {

    @GET
    public StatusPagamento[] buscarTodos() {
        return StatusPagamento.values();
    }
}

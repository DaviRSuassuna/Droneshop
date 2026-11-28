package br.com.droneshop.pagamento.resource;

import br.com.droneshop.pagamento.model.MetodoPagamento;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Listagem do enum (D31).
@PermitAll
@Path("metodos-pagamento")
@Produces(MediaType.APPLICATION_JSON)
public class MetodoPagamentoResource {

    @GET
    public MetodoPagamento[] buscarTodos() {
        return MetodoPagamento.values();
    }
}

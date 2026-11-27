package br.com.droneshop.endereco.resource;

import br.com.droneshop.endereco.dto.CepResponseDTO;
import br.com.droneshop.endereco.service.CepService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// GET /ceps/77001000 (aceita com ou sem hifen)
// 200 = encontrado | 400 = formato invalido | 404 = CEP nao existe | 503 = ViaCEP indisponivel
@PermitAll
@Path("ceps")
@Produces(MediaType.APPLICATION_JSON)
public class CepResource {

    @Inject
    CepService service;

    @GET
    @Path("/{cep}")
    public CepResponseDTO buscar(@PathParam("cep") String cep) {
        return service.buscar(cep);
    }

}

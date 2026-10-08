package br.com.droneshop.endereco.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// REST Client do Quarkus: o backend e "client" do ViaCEP (nada a ver com o Cliente comprador).
// URL e timeouts ficam em application.properties (quarkus.rest-client.viacep.*).
@RegisterRestClient(configKey = "viacep")
@Path("/ws")
@Produces(MediaType.APPLICATION_JSON)
public interface ViaCepClient {

    @GET
    @Path("/{cep}/json/")
    ViaCepResponse buscar(@PathParam("cep") String cep);

}

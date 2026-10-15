package br.com.droneshop.pedido;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Agregador de frete falso, que só existe nos testes (D34).
 *
 * No %test, quarkus.rest-client.frete.url aponta para cá. Devolve sempre dois
 * serviços dos Correios: PAC (mais barato e mais lento, vira PADRAO) e SEDEX
 * (mais rápido, vira EXPRESSA). CEP de destino começando com 99 simula o
 * agregador fora do ar (503). A rota só de teste GET /teste/ultima-requisicao
 * devolve o último pacote cotado, para conferir peso e dimensões.
 */
@Path("/frete-falso")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FreteFalso {

    public static final String CEP_AGREGADOR_FORA = "99010000";

    private static final AtomicReference<Map<String, Object>> ULTIMA = new AtomicReference<>(Map.of());

    @POST
    @Path("/api/v2/me/shipment/calculate")
    @SuppressWarnings("unchecked")
    public Response cotar(Map<String, Object> corpo) {
        ULTIMA.set(corpo);
        Map<String, Object> to = (Map<String, Object>) corpo.get("to");
        String cep = to == null ? "" : String.valueOf(to.get("postal_code")).replaceAll("\\D", "");
        if (cep.startsWith("99"))
            return Response.status(503).build();
        Map<String, Object> pacote = (Map<String, Object>) corpo.get("package");
        double peso = pacote == null ? 1 : Double.parseDouble(String.valueOf(pacote.get("weight")));
        return Response.ok(List.of(
                Map.of("id", 1, "name", "PAC", "price", Math.round((20 + peso * 5) * 100) / 100.0,
                        "delivery_time", 6, "company", Map.of("name", "Correios")),
                Map.of("id", 2, "name", "SEDEX", "price", Math.round((40 + peso * 8) * 100) / 100.0,
                        "delivery_time", 2, "company", Map.of("name", "Correios"))))
                .build();
    }

    @GET
    @Path("/teste/ultima-requisicao")
    public Map<String, Object> ultima() {
        return ULTIMA.get();
    }
}

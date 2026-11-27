package br.com.droneshop.pagamento;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Mercado Pago falso, que só existe nos testes (D34).
 *
 * No %test, quarkus.rest-client.mercadopago.url aponta para cá. Imita só o
 * pedaço da API que a #27 e a #36 usam:
 * - POST /v1/payments: cartão "tok_recusado" é recusado, "tok_gateway_fora" dá
 *   erro 500, qualquer outro cartão é aprovado; PIX e boleto ficam pendentes;
 * - GET /v1/payments/{id}: status atual (é o que o webhook consulta);
 * - POST /v1/payments/{id}/refunds: estorno sempre aprovado.
 * Rota só de teste: POST /teste/{id}/status muda o status, como se o cliente
 * tivesse pago o PIX.
 */
@PermitAll
@Path("/mercadopago-falso")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MercadoPagoFalso {

    public static final String CARTAO_APROVADO = "tok_aprovado";
    public static final String CARTAO_RECUSADO = "tok_recusado";
    public static final String GATEWAY_FORA = "tok_gateway_fora";

    private static final AtomicLong SEQ = new AtomicLong(9_000_000);
    private static final Map<String, Map<String, Object>> PAGAMENTOS = new ConcurrentHashMap<>();

    @POST
    @Path("/v1/payments")
    public Response criar(Map<String, Object> corpo) {
        String metodo = String.valueOf(corpo.get("payment_method_id"));
        String token = String.valueOf(corpo.get("token"));
        if (GATEWAY_FORA.equals(token))
            return Response.serverError().build();
        String status;
        if ("pix".equals(metodo) || "bolbradesco".equals(metodo) || "account_money".equals(metodo))
            status = "pending";
        else
            status = CARTAO_RECUSADO.equals(token) ? "rejected" : "approved";
        String id = String.valueOf(SEQ.incrementAndGet());
        Map<String, Object> p = new HashMap<>(corpo);
        p.put("id", id);
        p.put("status", status);
        PAGAMENTOS.put(id, p);
        return Response.ok(p).build();
    }

    @GET
    @Path("/v1/payments/{id}")
    public Response consultar(@PathParam("id") String id) {
        Map<String, Object> p = PAGAMENTOS.get(id);
        return p == null ? Response.status(404).build() : Response.ok(p).build();
    }

    @POST
    @Path("/v1/payments/{id}/refunds")
    public Response estornar(@PathParam("id") String id, Map<String, Object> corpo) {
        if (!PAGAMENTOS.containsKey(id))
            return Response.status(404).build();
        return Response.ok(Map.of("id", SEQ.incrementAndGet(), "payment_id", id,
                "amount", corpo.get("amount"), "status", "approved")).build();
    }

    @POST
    @Path("/teste/{id}/status")
    public Response mudarStatus(@PathParam("id") String id, Map<String, Object> corpo) {
        Map<String, Object> p = PAGAMENTOS.get(id);
        if (p == null)
            return Response.status(404).build();
        p.put("status", corpo.get("status"));
        return Response.noContent().build();
    }
}

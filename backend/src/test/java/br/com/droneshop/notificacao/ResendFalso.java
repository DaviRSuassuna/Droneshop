package br.com.droneshop.notificacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Resend falso, que só existe nos testes (D34).
 *
 * No %test, quarkus.rest-client.resend.url aponta para cá. POST /emails guarda
 * o e-mail e devolve o id, como o Resend; destinatário com "falha-envio" no
 * endereço dá erro 500. A rota só de teste GET /emails?para= lista o que foi
 * enviado para um endereço, do mais recente para o mais antigo.
 */
@PermitAll
@Path("/resend-falso")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResendFalso {

    private static final List<Map<String, Object>> ENVIADOS = Collections.synchronizedList(new ArrayList<>());

    @POST
    @Path("/emails")
    public Response enviar(Map<String, Object> corpo) {
        if (String.valueOf(corpo.get("to")).contains("falha-envio"))
            return Response.serverError().build();
        Map<String, Object> e = new HashMap<>(corpo);
        String id = UUID.randomUUID().toString();
        e.put("id", id);
        ENVIADOS.add(0, e);
        return Response.ok(Map.of("id", id)).build();
    }

    @GET
    @Path("/emails")
    public List<Map<String, Object>> listar(@QueryParam("para") String para) {
        synchronized (ENVIADOS) {
            return ENVIADOS.stream().filter(e -> String.valueOf(e.get("to")).contains(para)).toList();
        }
    }
}

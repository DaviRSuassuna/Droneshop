package br.com.droneshop.pedido.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.pedido.model.NotaFiscal;
import br.com.droneshop.pedido.service.NotaFiscalService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/** XML e DANFE da nota: o admin vê todas; o cliente, só as dos próprios pedidos (#35). */
@Path("notas-fiscais/{id}")
@RolesAllowed({ "ADMINISTRATIVO", "CLIENTE" })
public class ArquivoNotaFiscalResource {

    @Inject
    NotaFiscalService notas;

    @Inject
    SecurityIdentity identidade;

    @Inject
    JsonWebToken token;

    private NotaFiscal permitida(Long id) {
        NotaFiscal n = notas.buscar(id);
        boolean dono = n.getPedido().getCliente().getUsuario().getId().equals(Long.valueOf(token.getSubject()));
        if (!identidade.hasRole("ADMINISTRATIVO") && !dono)
            throw new ResourceNotFoundException("Nota fiscal não encontrada.");
        return n;
    }

    @GET
    @Path("xml")
    @Produces("application/xml")
    @Transactional
    public Response xml(@PathParam("id") Long id) {
        NotaFiscal n = permitida(id);
        return Response.ok(notas.xml(id), "application/xml")
                .header("Content-Disposition", "inline; filename=\"nfe-" + n.getNumero() + ".xml\"").build();
    }

    @GET
    @Path("danfe")
    @Produces("application/pdf")
    @Transactional
    public Response danfe(@PathParam("id") Long id) {
        NotaFiscal n = permitida(id);
        return Response.ok(notas.danfe(id), "application/pdf")
                .header("Content-Disposition", "inline; filename=\"danfe-" + n.getNumero() + ".pdf\"").build();
    }
}

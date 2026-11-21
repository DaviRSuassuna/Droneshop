package br.com.droneshop.notificacao.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.notificacao.dto.NotificacaoResponseDTO;
import br.com.droneshop.notificacao.dto.TemplateEmailRequestDTO;
import br.com.droneshop.notificacao.dto.TemplateEmailResponseDTO;
import br.com.droneshop.notificacao.service.NotificacaoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.NOTIFICACAO)
public class TemplateEmailResource {

    @Inject
    NotificacaoService service;

    @GET
    @Path("templates-email")
    public List<TemplateEmailResponseDTO> templates() {
        return service.templates();
    }

    @GET
    @Path("templates-email/{codigo}")
    public TemplateEmailResponseDTO template(@PathParam("codigo") String codigo) {
        return service.template(codigo);
    }

    @PUT
    @Path("templates-email/{codigo}")
    public TemplateEmailResponseDTO editar(@PathParam("codigo") String codigo, @Valid TemplateEmailRequestDTO dto) {
        return service.editar(codigo, dto);
    }

    @GET
    @Path("notificacoes")
    public PageResponse<NotificacaoResponseDTO> notificacoes(@QueryParam("destinatario") String destinatario,
            @QueryParam("idStatusEnvio") Long idStatusEnvio,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return service.listar(destinatario, idStatusEnvio, page, pageSize);
    }
}

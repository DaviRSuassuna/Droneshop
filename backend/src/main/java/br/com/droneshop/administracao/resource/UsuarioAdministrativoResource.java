package br.com.droneshop.administracao.resource;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.dto.UsuarioAdministrativoRequestDTO;
import br.com.droneshop.administracao.dto.UsuarioAdministrativoResponseDTO;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.administracao.service.UsuarioAdministrativoService;
import br.com.droneshop.dto.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("admin/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ADMINISTRACAO)
public class UsuarioAdministrativoResource {

    @Inject
    UsuarioAdministrativoService service;

    @Inject
    UsuarioAtual usuarioAtual;

    @POST
    public UsuarioAdministrativoResponseDTO criar(@Valid UsuarioAdministrativoRequestDTO dto) {
        return service.criar(dto);
    }

    @GET
    public PageResponse<UsuarioAdministrativoResponseDTO> listar(
            @QueryParam("nome") String nome,
            @QueryParam("idPapel") Long idPapel,
            @QueryParam("ativo") Boolean ativo,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.listar(nome, idPapel, ativo, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public UsuarioAdministrativoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return service.buscarPorId(id);
    }

    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") Long id, @Valid UsuarioAdministrativoRequestDTO dto) {
        Long currentUserId = usuarioAtual.id();
        service.atualizar(id, dto, currentUserId);
        return Response.noContent().build();
    }
}

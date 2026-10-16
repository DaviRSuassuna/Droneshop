package br.com.droneshop.administracao.resource;

import java.util.List;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.dto.PermissaoItemResponseDTO;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.administracao.service.UsuarioAdministrativoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("admin/papeis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.ADMINISTRACAO)
public class PapelPermissaoPadraoResource {

    @Inject
    UsuarioAdministrativoService service;

    @GET
    @Path("/{idPapel}/permissoes-padrao")
    public List<PermissaoItemResponseDTO> permissoesPadrao(@PathParam("idPapel") Long idPapel) {
        return service.permissoesPadraoDoPapel(idPapel);
    }
}

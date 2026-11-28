package br.com.droneshop.devolucao.resource;

import java.util.List;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.devolucao.dto.DevolucaoRequestDTO;
import br.com.droneshop.devolucao.dto.DevolucaoResponseDTO;
import br.com.droneshop.devolucao.service.DevolucaoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Devoluções do cliente logado (#37). */
@Path("conta/devolucoes")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class ContaDevolucaoResource {

    @Inject
    UsuarioAtual usuarioAtual;

    @Inject
    DevolucaoService service;

    private Long usuario() {
        return usuarioAtual.id();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public DevolucaoResponseDTO solicitar(@Valid DevolucaoRequestDTO dto) {
        return DevolucaoResponseDTO.de(service.solicitar(usuario(), dto));
    }

    @GET
    @Transactional
    public List<DevolucaoResponseDTO> listar() {
        return service.doCliente(usuario()).stream().map(DevolucaoResponseDTO::de).toList();
    }

    @GET
    @Path("{id}")
    @Transactional
    public DevolucaoResponseDTO detalhe(@PathParam("id") Long id) {
        return DevolucaoResponseDTO.de(service.doCliente(id, usuario()));
    }
}

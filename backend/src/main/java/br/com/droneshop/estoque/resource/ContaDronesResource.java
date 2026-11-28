package br.com.droneshop.estoque.resource;

import java.util.List;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.estoque.dto.DroneDaContaResponseDTO;
import br.com.droneshop.estoque.dto.RegistroAnacRequestDTO;
import br.com.droneshop.estoque.repository.UnidadeIndividualRepository;
import br.com.droneshop.estoque.service.UnidadeIndividualService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** "Meus drones": unidades compradas pelo cliente, com série, garantia e registro ANAC (#88). */
@Path("conta/drones")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
public class ContaDronesResource {

    @Inject
    UsuarioAtual usuarioAtual;

    @Inject
    UnidadeIndividualRepository unidades;

    @Inject
    UnidadeIndividualService service;

    @GET
    @Transactional
    public List<DroneDaContaResponseDTO> listar() {
        return unidades.doCliente(usuarioAtual.id()).stream().map(DroneDaContaResponseDTO::de)
                .toList();
    }

    @PATCH
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarAnac(@PathParam("id") Long id, RegistroAnacRequestDTO dto) {
        service.registrarAnac(usuarioAtual.id(), id, dto == null ? null : dto.registroAnac());
        return Response.noContent().build();
    }
}

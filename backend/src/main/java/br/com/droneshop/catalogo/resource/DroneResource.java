package br.com.droneshop.catalogo.resource;

import br.com.droneshop.catalogo.dto.DroneRequestDTO;
import br.com.droneshop.catalogo.dto.DroneResponseDTO;
import br.com.droneshop.catalogo.mapper.ProdutoResponseMapper;
import br.com.droneshop.catalogo.service.DroneService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Consulta, listagem e exclusao ficam em /produtos (D27)
@Path("drones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
public class DroneResource {

    @Inject
    DroneService service;

    @POST
    public DroneResponseDTO incluir(DroneRequestDTO dto) {
        return ProdutoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, DroneRequestDTO dto) {
        service.update(id, dto);
    }

}

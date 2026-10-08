package br.com.droneshop.endereco.resource;

import java.util.List;

import br.com.droneshop.endereco.dto.EstadoResponseDTO;
import br.com.droneshop.endereco.model.Estado;
import br.com.droneshop.endereco.service.EstadoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Somente leitura: dados oficiais do IBGE, usados no select de Estado.
@Path("estados")
@Produces(MediaType.APPLICATION_JSON)
public class EstadoResource {

    @Inject
    EstadoService service;

    @GET
    public List<EstadoResponseDTO> buscarTodos() {
        return service.findAll().stream().map(EstadoResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/{id}")
    public EstadoResponseDTO buscarPorId(@PathParam("id") Long id) {
        Estado estado = service.findById(id);
        return (estado == null) ? null : EstadoResponseDTO.valueOf(estado);
    }

}

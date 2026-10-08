package br.com.droneshop.endereco.resource;

import java.util.List;

import br.com.droneshop.endereco.dto.MunicipioResponseDTO;
import br.com.droneshop.endereco.model.Municipio;
import br.com.droneshop.endereco.service.MunicipioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Somente leitura: dados oficiais do IBGE, usados no select de Municipio.
@Path("municipios")
@Produces(MediaType.APPLICATION_JSON)
public class MunicipioResource {

    @Inject
    MunicipioService service;

    // GET /municipios?idEstado=7 -> municipios do estado, em ordem alfabetica
    @GET
    public List<MunicipioResponseDTO> buscarPorEstado(@QueryParam("idEstado") Long idEstado) {
        return service.findByEstado(idEstado).stream().map(MunicipioResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/{id}")
    public MunicipioResponseDTO buscarPorId(@PathParam("id") Long id) {
        Municipio municipio = service.findById(id);
        return (municipio == null) ? null : MunicipioResponseDTO.valueOf(municipio);
    }

}

package br.com.droneshop.endereco.resource;

import java.util.List;

import br.com.droneshop.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import br.com.droneshop.endereco.dto.MunicipioResponseDTO;
import br.com.droneshop.endereco.mapper.MunicipioResponseMapper;
import br.com.droneshop.endereco.model.Municipio;
import br.com.droneshop.endereco.service.MunicipioService;

// Somente leitura: dados oficiais do IBGE, usados no select de Municipio.
@Path("municipios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MunicipioResource {

    @Inject
    MunicipioService service;

    // GET /municipios?idEstado=7 -> municipios do estado, em ordem alfabetica
    @GET
    public PageResponse<MunicipioResponseDTO> buscarPorEstado(@QueryParam("idEstado") Long idEstado,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Municipio> municipios = service.findByEstado(idEstado, page, pageSize);
        long totalItems = service.count(idEstado);

        return PageResponse.of(municipios, page, pageSize, totalItems, MunicipioResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<MunicipioResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Municipio> municipios = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(municipios, page, pageSize, totalItems, MunicipioResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public MunicipioResponseDTO buscarPorId(@PathParam("id") Long id) {
        return MunicipioResponseMapper.toResponse(service.findById(id));
    }

}

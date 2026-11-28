package br.com.droneshop.endereco.resource;

import java.util.List;

import br.com.droneshop.dto.PageResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import br.com.droneshop.endereco.dto.EstadoResponseDTO;
import br.com.droneshop.endereco.mapper.EstadoResponseMapper;
import br.com.droneshop.endereco.model.Estado;
import br.com.droneshop.endereco.service.EstadoService;

// Somente leitura: dados oficiais do IBGE, usados no select de Estado.
@PermitAll
@Path("estados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EstadoResource {

    @Inject
    EstadoService service;

    @GET
    public PageResponse<EstadoResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Estado> estados = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(estados, page, pageSize, totalItems, EstadoResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<EstadoResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Estado> estados = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(estados, page, pageSize, totalItems, EstadoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public EstadoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return EstadoResponseMapper.toResponse(service.findById(id));
    }

    @GET
    @Path("/sigla/{sigla}")
    public EstadoResponseDTO buscarPorSigla(@PathParam("sigla") String sigla) {
        return EstadoResponseMapper.toResponse(service.findBySigla(sigla));
    }

}

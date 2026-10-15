package br.com.droneshop.catalogo.resource;

import br.com.droneshop.catalogo.dto.AcessorioRequestDTO;
import br.com.droneshop.catalogo.dto.AcessorioResponseDTO;
import br.com.droneshop.catalogo.mapper.ProdutoResponseMapper;
import br.com.droneshop.catalogo.service.AcessorioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Consulta, listagem e exclusao ficam em /produtos (D27)
@Path("acessorios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AcessorioResource {

    @Inject
    AcessorioService service;

    @POST
    public AcessorioResponseDTO incluir(AcessorioRequestDTO dto) {
        return ProdutoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, AcessorioRequestDTO dto) {
        service.update(id, dto);
    }

}

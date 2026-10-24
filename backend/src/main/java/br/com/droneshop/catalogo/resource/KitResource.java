package br.com.droneshop.catalogo.resource;

import br.com.droneshop.catalogo.dto.KitRequestDTO;
import br.com.droneshop.catalogo.dto.KitResponseDTO;
import br.com.droneshop.catalogo.mapper.ProdutoResponseMapper;
import br.com.droneshop.catalogo.service.KitService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;

// Consulta, listagem e exclusao ficam em /produtos (D27)
@Path("kits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.CATALOGO)
public class KitResource {

    @Inject
    KitService service;

    @POST
    public KitResponseDTO incluir(KitRequestDTO dto) {
        return ProdutoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, KitRequestDTO dto) {
        service.update(id, dto);
    }

}

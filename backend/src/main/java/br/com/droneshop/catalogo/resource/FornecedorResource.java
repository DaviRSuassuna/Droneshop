package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import br.com.droneshop.catalogo.dto.FornecedorRequestDTO;
import br.com.droneshop.catalogo.dto.FornecedorResponseDTO;
import br.com.droneshop.catalogo.mapper.FornecedorResponseMapper;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.service.FornecedorService;

@Path("fornecedores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FornecedorResource {

    @Inject
    FornecedorService service;

    @GET
    public PageResponse<FornecedorResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Fornecedor> fornecedors = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(fornecedors, page, pageSize, totalItems, FornecedorResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<FornecedorResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Fornecedor> fornecedors = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(fornecedors, page, pageSize, totalItems, FornecedorResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public FornecedorResponseDTO buscarPorId(@PathParam("id") Long id) {
        return FornecedorResponseMapper.toResponse(service.findById(id));
    }

    // aceita com ou sem mascara; na URL use so os 14 digitos (a mascara tem "/")
    @GET
    @Path("/cnpj/{cnpj}")
    public FornecedorResponseDTO buscarPorCnpj(@PathParam("cnpj") String cnpj) {
        return FornecedorResponseMapper.toResponse(service.findByCnpj(cnpj));
    }

    @POST
    public FornecedorResponseDTO incluir(FornecedorRequestDTO dto) {
        return FornecedorResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, FornecedorRequestDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}

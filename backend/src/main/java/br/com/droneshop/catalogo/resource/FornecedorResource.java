package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.dto.FornecedorDTO;
import br.com.droneshop.catalogo.dto.FornecedorResponseDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.service.FornecedorService;
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

@Path("fornecedores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FornecedorResource {

    @Inject
    FornecedorService service;

    // Fornecedor e serializado via FornecedorResponseDTO, igual CategoriaResource.

    @GET
    public List<FornecedorResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findAll(page, pageSize).stream().map(FornecedorResponseDTO::valueOf).toList();
    }

    // "nome" = razao social
    @GET
    @Path("/nome/{nome}")
    public List<FornecedorResponseDTO> buscarPorNome(@PathParam("nome") String nome, @QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findByNome(nome, page, pageSize).stream().map(FornecedorResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/{id}")
    public FornecedorResponseDTO buscarPorId(@PathParam("id") Long id) {
        Fornecedor fornecedor = service.findById(id);
        return (fornecedor == null) ? null : FornecedorResponseDTO.valueOf(fornecedor);
    }

    // CNPJ aceito com ou sem mascara (a mascara tem "/", entao na URL use so os 14 digitos)
    @GET
    @Path("/cnpj/{cnpj}")
    public FornecedorResponseDTO buscarPorCnpj(@PathParam("cnpj") String cnpj) {
        Fornecedor fornecedor = service.findByCnpj(cnpj);
        return (fornecedor == null) ? null : FornecedorResponseDTO.valueOf(fornecedor);
    }

    @POST
    public FornecedorResponseDTO incluir(FornecedorDTO dto) {
        return FornecedorResponseDTO.valueOf(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, FornecedorDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

    @GET
    @Path("/count")
    public Long total() {
        return service.count();
    }

    @GET
    @Path("/nome/{nome}/count")
    public Long totalPorNome(@PathParam("nome") String nome) {
        return service.count(nome);
    }

}

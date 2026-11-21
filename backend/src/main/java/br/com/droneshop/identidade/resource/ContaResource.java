package br.com.droneshop.identidade.resource;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.identidade.dto.EnderecoClienteRequestDTO;
import br.com.droneshop.identidade.dto.EnderecoClienteResponseDTO;
import br.com.droneshop.identidade.dto.PerfilClienteResponseDTO;
import br.com.droneshop.identidade.dto.PerfilRequestDTO;
import br.com.droneshop.identidade.dto.SenhaRequestDTO;
import br.com.droneshop.identidade.mapper.ClienteMapper;
import br.com.droneshop.identidade.mapper.EnderecoClienteMapper;
import br.com.droneshop.identidade.service.ClienteService;
import br.com.droneshop.identidade.service.EnderecoClienteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Área do cliente logado: sem token 401, token sem perfil de cliente 403.
@Path("conta")
@RolesAllowed("CLIENTE")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContaResource {

    @Inject
    JsonWebToken token;

    @Inject
    ClienteService clientes;

    @Inject
    EnderecoClienteService enderecos;

    private Long usuario() {
        return Long.valueOf(token.getSubject());
    }

    @GET
    @Path("perfil")
    public PerfilClienteResponseDTO perfil() {
        return ClienteMapper.toPerfil(clientes.perfilDoUsuario(usuario()));
    }

    @PUT
    @Path("perfil")
    public Response alterarPerfil(@Valid PerfilRequestDTO dto) {
        clientes.alterarPerfil(usuario(), dto);
        return Response.noContent().build();
    }

    @PUT
    @Path("senha")
    public Response trocarSenha(@Valid SenhaRequestDTO dto) {
        clientes.trocarSenha(usuario(), dto);
        return Response.noContent().build();
    }

    @GET
    @Path("enderecos")
    public List<EnderecoClienteResponseDTO> enderecos() {
        return enderecos.listar(usuario()).stream().map(EnderecoClienteMapper::toResponse).toList();
    }

    @POST
    @Path("enderecos")
    public EnderecoClienteResponseDTO criarEndereco(@Valid EnderecoClienteRequestDTO dto) {
        return EnderecoClienteMapper.toResponse(enderecos.criar(usuario(), dto));
    }

    @PUT
    @Path("enderecos/{id}")
    public Response alterarEndereco(@PathParam("id") Long id, @Valid EnderecoClienteRequestDTO dto) {
        enderecos.alterar(usuario(), id, dto);
        return Response.noContent().build();
    }

    @DELETE
    @Path("enderecos/{id}")
    public Response excluirEndereco(@PathParam("id") Long id) {
        enderecos.excluir(usuario(), id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("enderecos/{id}/principal")
    public Response marcarPrincipal(@PathParam("id") Long id) {
        enderecos.marcarPrincipal(usuario(), id);
        return Response.noContent().build();
    }
}

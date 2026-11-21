package br.com.droneshop.identidade.resource;

import br.com.droneshop.identidade.dto.ClienteResponseDTO;
import br.com.droneshop.identidade.dto.ContaRequestDTO;
import br.com.droneshop.identidade.mapper.ClienteMapper;
import br.com.droneshop.identidade.service.ClienteService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Público: criação de conta do cliente (RF48).
@Path("clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    @Inject
    ClienteService clientes;

    @POST
    public ClienteResponseDTO criarConta(@Valid ContaRequestDTO dto) {
        return ClienteMapper.toConta(clientes.criarConta(dto));
    }
}

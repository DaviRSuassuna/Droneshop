package br.com.droneshop.identidade.service;

import java.util.List;

import br.com.droneshop.identidade.dto.EnderecoClienteRequestDTO;
import br.com.droneshop.identidade.model.EnderecoCliente;
import jakarta.validation.Valid;

// Sempre no escopo do cliente logado: endereço de outro cliente -> 404.
public interface EnderecoClienteService {

    List<EnderecoCliente> listar(Long idUsuario);

    EnderecoCliente criar(Long idUsuario, @Valid EnderecoClienteRequestDTO dto);

    void alterar(Long idUsuario, Long id, @Valid EnderecoClienteRequestDTO dto);

    void excluir(Long idUsuario, Long id);

    void marcarPrincipal(Long idUsuario, Long id);
}

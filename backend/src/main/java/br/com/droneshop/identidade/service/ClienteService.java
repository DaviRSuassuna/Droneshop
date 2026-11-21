package br.com.droneshop.identidade.service;

import br.com.droneshop.identidade.dto.ContaRequestDTO;
import br.com.droneshop.identidade.dto.PerfilRequestDTO;
import br.com.droneshop.identidade.dto.SenhaRequestDTO;
import br.com.droneshop.identidade.model.PerfilCliente;
import jakarta.validation.Valid;

public interface ClienteService {

    PerfilCliente criarConta(@Valid ContaRequestDTO dto);

    // perfil do usuário logado; sem perfil de cliente -> 403
    PerfilCliente perfilDoUsuario(Long idUsuario);

    void alterarPerfil(Long idUsuario, @Valid PerfilRequestDTO dto);

    void trocarSenha(Long idUsuario, @Valid SenhaRequestDTO dto);
}
